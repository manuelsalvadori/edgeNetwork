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
import io.grpc.stub.StreamObserver;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import simulation.Measurement;
import edge_nodes.NodeGRPCOuterClass.Statistic;

import java.util.Calendar;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
    private HashSet<String> localNodesList;
    private PriorityQueue<Measurement> queue;
    private String CoordURI;
    private CoordinatorThread coordinatorThread;

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

    public String getServerURI()
    {
        return serverURI;
    }

    public HashSet<String> getLocalNodesList()
    {
        return localNodesList;
    }

    public void setLocalNodesList(HashSet<String> localNodesList)
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
                    this.localNodesList = new HashSet<>();
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

    private long computeMidnightMillis()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
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
            System.out.println("MEAN: "+mean+" at "+computeMidnightMillis());
            sendLocalStatistic(Statistic.newBuilder().setNodeID(id).setValue(mean).setTimestamp(computeMidnightMillis()).build());
        }
    }

    public void sendLocalStatistic(Statistic s)
    {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(CoordURI).usePlaintext(true).build();
        NodeGRPCGrpc.NodeGRPCBlockingStub stub = NodeGRPCGrpc.newBlockingStub(channel);

        try
        {
            stub.sendStatistic(s);
        }
        catch(StatusRuntimeException e)
        {
            System.out.println("Coord lost");
            //elezione nuovo coordinatore
        }

        channel.shutdown();

    }

    public void reportToEdgeNetwork(HashSet<EdgeNode> nodeList)
    {
        if(nodeList.size() == 0)
        {
            this.setIsCoordinator(true);
            this.CoordURI = this.nodeURI+":"+this.nodesPort;
            this.coordinatorThread = new CoordinatorThread();
            Thread t = new Thread(this.coordinatorThread);
            t.start();
            System.out.println(this.getId() + " - I am the coordinator");
            return;
        }

        for(EdgeNode node: nodeList)
        {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(node.nodeURI+":"+node.getNodesPort()).usePlaintext(true).build();

            NodeGRPCGrpc.NodeGRPCStub stub = NodeGRPCGrpc.newStub(channel);

            NodeGRPCOuterClass.NodeURI uri = NodeGRPCOuterClass.NodeURI.newBuilder().setNode(this.nodeURI+":"+this.nodesPort).build();
            try
            {
                stub.reportNewNode(uri, new StreamObserver<NodeGRPCOuterClass.Coordinator>()
                {
                    @Override
                    public void onNext(NodeGRPCOuterClass.Coordinator coordinator)
                    {
                        if (coordinator.getIsCoord())
                            CoordURI = node.nodeURI + ":" + node.getNodesPort();
                        else
                            localNodesList.add(node.nodeURI + ":" + node.getNodesPort());
                    }

                    @Override
                    public void onError(Throwable throwable)
                    {
                        System.out.println("Error! " + throwable.getMessage());
                    }

                    @Override
                    public void onCompleted()
                    {
                        channel.shutdownNow();
                    }
                });
            }
            catch (StatusRuntimeException e)
            {
                localNodesList.remove(node.nodeURI + ":" + node.getNodesPort());
                e.printStackTrace();
            }

            try
            {
                channel.awaitTermination(2, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {e.printStackTrace();}

        }

        System.out.println(this.getId() + " - My coordinator is "+CoordURI);
    }

    public synchronized void addNodeToLocalList(String uri)
    {
        localNodesList.add(uri);
    }

}