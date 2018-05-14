package edge_nodes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import simulation.Measurement;
import edge_nodes.NodeGRPCOuterClass.Statistic;

import java.util.*;

public class EdgeNode
{
    private String id;
    private String nodeURI;
    private int sensorsPort;
    private int nodesPort;
    private boolean isCoordinator;
    private int x;
    private int y;
    private String serverURI;
    private HashMap<String,String> localNodesList;
    private PriorityQueue<Measurement> queue;
    private String CoordURI;
    private CoordinatorThread coordinatorThread;
    private volatile int grpcCounter;

    public EdgeNode(String id, String serverURI, int sensorsPort, int nodesPort)
    {
        this.id = id;
        this.nodeURI = "localhost";
        this.sensorsPort = sensorsPort;
        this.nodesPort = nodesPort;
        this.isCoordinator = false;
        this.serverURI = serverURI;
    }

    public String getId()
    {
        return id;
    }

    public String getNodeURI()
    {
        return nodeURI;
    }

    public int getSensorsPort()
    {
        return sensorsPort;
    }

    public int getNodesPort()
    {
        return nodesPort;
    }

    public int getY()
    {
        return y;
    }

    public int getX()
    {
        return x;
    }

    public boolean getIsCoordinator()
    {
        return isCoordinator;
    }

    public CoordinatorThread getCoordinatorThread()
    {
        return coordinatorThread;
    }

    public void setIsCoordinator(boolean isCoordinator)
    {
        this.isCoordinator = isCoordinator;
    }

    public void setCoordURI(String uri)
    {
        this.CoordURI = uri;
    }

    public String getServerURI()
    {
        return serverURI;
    }

    public HashMap<String,String> getLocalNodesList()
    {
        return localNodesList;
    }

    public void setLocalNodesList(HashMap<String,String> localNodesList)
    {
        this.localNodesList = localNodesList;
    }

    public boolean nodeInit()
    {
        Client restClient = restClientInit();
        int i = 2;
        do
        {
            Random rnd = new Random(System.currentTimeMillis());
            this.x = rnd.nextInt(99);
            this.y = rnd.nextInt(99);

            System.out.println(this.getId() + " (" + x + "," + y + ") - Node initialization...");
            ClientResponse response;
            try
            {
                WebResource webResource = restClient.resource(this.serverURI + "/NodeInit/");
                String json = new Gson().toJson(this);
                response = webResource.type("application/json").post(ClientResponse.class, json);
            }
            catch (ClientHandlerException ce)
            {
                System.out.println(this.getId() + " - Server cloud connection refused - init impossible");
                return false;
            }

            switch (response.getStatus())
            {
                case 200:
                    String json = response.getEntity(String.class);
                    this.queue = new PriorityQueue<>(40);
                    this.localNodesList = new HashMap<>();
                    reportToEdgeNetwork(new Gson().fromJson(json, new TypeToken<HashSet<EdgeNode>>(){}.getType()));
                    System.out.println(this.getId() + " - Successfully added to the cloud");
                    return true;

                case 403:
                    System.out.println(this.getId() + " - " + response.getEntity(String.class));
                    System.out.println("Retrying: attempt " + i + "/10");
                    break;

                case 400:
                    System.out.println(this.getId() + " - ERROR " + response.getEntity(String.class));
                    return false;

                default:
                    System.out.println(this.getId() + " - ERROR Failed node init: HTTP error code: " + response.getStatus());
            }
        }while(i++ < 10);
        return false;
    }

    private Client restClientInit()
    {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        System.out.println(this.getId()+ " - REST client configured");
        return Client.create(config);
    }

    public long computeTimestamp()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return System.currentTimeMillis() - cal.getTimeInMillis();
    }

    public synchronized void addMeasurement(Measurement m)
    {
        queue.offer(m);
        if(queue.size() == 40)
        {
            double mean = 0;
            for (int i = 0; i < 20; i++)
                mean += queue.poll().getValue();
            for (int i = 0; i < 20; i++)
                mean += queue.peek().getValue();
            mean /= 40;
            System.out.println("MEAN: "+mean+" at "+ computeTimestamp());
            sendLocalStatistic(Statistic.newBuilder().setNodeID(id).setValue(mean).setTimestamp(computeTimestamp()).build());
        }
    }

    public void sendLocalStatistic(Statistic s)
    {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(CoordURI).usePlaintext(true).build();
        NodeGRPCGrpc.NodeGRPCBlockingStub stub = NodeGRPCGrpc.newBlockingStub(channel);

        try
        {
            stub.sendStatistic(s);
            // salvo la stat lastGlobaStats
        }
        catch(StatusRuntimeException e)
        {
            System.out.println("Coordinator offline - starting new election...");
            //elezione nuovo coordinatore
        }
        channel.shutdown();
    }

    public void reportToEdgeNetwork(HashSet<EdgeNode> nodeList)
    {
        int size = nodeList.size();

        // se la lista dei nodi ricevuta è vuota setto il nodo this come coordinatore
        if(size == 0)
        {
            this.setIsCoordinator(true);
            this.CoordURI = this.nodeURI+":"+this.nodesPort;
            this.coordinatorThread = new CoordinatorThread(this);
            Thread t = new Thread(this.coordinatorThread);
            t.start();
            System.out.println(this.getId() + " - I am the coordinator");
            return;
        }

        // lancio parallelo di gRPC
        System.out.println(this.getId() + " - Retrieving coordinator...");

        grpcCounter = size;
        int i = 0;
        for(EdgeNode node: nodeList)
        {
            System.out.println("    gRPC call "+(++i)+": Asking to node "+ node.getId()+"...");
            new Thread(new ParallelGrpcCoordFinder(this, node.getId(),node.nodeURI+":"+node.getNodesPort(), i)).start();
        }

        // aspetto che tutte le chiamate gRPC ritornino
        while(grpcCounter > 0);

        // stampo il coordinatore ricevuto
        System.out.println(this.getId() + " - My coordinator is "+CoordURI);
    }

    public synchronized void addNodeToLocalList(String id, String uri)
    {
        localNodesList.put(id, uri);
    }

    public synchronized void removeNodeFromLocalList(String id)
    {
        localNodesList.remove(id);
    }

    public synchronized void decCounter()
    {
        this.grpcCounter--;
    }

}