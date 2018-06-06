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
import java.util.stream.Collectors;

public class EdgeNode
{
    private final String id;
    private final String nodeURI;
    private final int sensorsPort;
    private final int nodesPort;
    private boolean isCoordinator;
    private int x;
    private int y;
    private final String serverURI;
    private HashMap<String,String> localNodesList;
    private PriorityQueue<Measurement> buffer;
    private PriorityQueue<Statistic> tmp_buffer;
    private Statistic lastGlobalStat;
    private String CoordURI;
    private CoordinatorThread coordinatorThread;
    private WaitForOKs waitOKs = null;

    public EdgeNode(String id, String serverURI, int sensorsPort, int nodesPort)
    {
        this.id = id;
        this.nodeURI = "localhost";
        this.sensorsPort = sensorsPort;
        this.nodesPort = nodesPort;
        this.isCoordinator = false;
        this.serverURI = serverURI;
        this.tmp_buffer = new PriorityQueue<>(Comparator.comparingLong(Statistic::getTimestamp));
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

    public WaitForOKs getWaitForOks()
    {
        return waitOKs;
    }

    private void setIsCoordinator()
    {
        this.isCoordinator = true;
    }

    public void setCoordURI(String uri)
    {
        this.CoordURI = uri;
    }

    public String getServerURI()
    {
        return serverURI;
    }

    public Statistic getLastGlobalStat()
    {
        return lastGlobalStat;
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
                WebResource webResource = restClient.resource(this.serverURI + "/Node/" + this.id + "/");
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
                    this.buffer = new PriorityQueue<>(40);
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

    void removeFromCloud()
    {
        Client restClient = restClientInit();

            ClientResponse response;
            try
            {
                WebResource webResource = restClient.resource(this.serverURI + "/Node/" + this.id + "/");
                response = webResource.delete(ClientResponse.class);
            }
            catch (ClientHandlerException ce)
            {
                System.out.println(this.getId() + " - Server cloud connection refused");
                return;
            }

            switch(response.getStatus())
            {
                case 200:
                    System.out.println(this.getId() + " - Successfully removed to the cloud");
                    return;
                case 404:
                    System.out.println(this.getId() + " - " + response.getEntity(String.class));
                    return;
                default:
                    System.out.println(this.getId() + " - ERROR Failed node init: HTTP error code: " + response.getStatus());
            }
        }

    private Client restClientInit()
    {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        return Client.create(config);
    }

    long computeTimestamp()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return System.currentTimeMillis() - cal.getTimeInMillis();
    }

    synchronized void addMeasurement(Measurement m)
    {
//        // DEBUG - test concorrenza
//        System.out.println("+ **** DEBUG - addMeasure() sleeping ****");
//        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
//        System.out.println("- **** DEBUG - addMeasure() awaked ****");

        buffer.offer(m);

        // alla 40sima misurazione faccio la media tramite
        // sliding window e la mando al coordinatore

        int bufferSize = 40;
        if(buffer.size() == bufferSize)
        {
            double mean = 0;

            for (int i = 0; i < bufferSize/2; i++)
                mean += buffer.poll().getValue();

            mean += buffer.stream().mapToDouble(Measurement::getValue).sum();
            mean /= (double)bufferSize;

            System.out.println(getId() + " - localStat:      " + String.format("%.14f",mean) + " at "+ computeTimestamp());

            sendLocalStatistic(Statistic.newBuilder()
                    .setNodeID(id)
                    .setValue(mean)
                    .setTimestamp(computeTimestamp())
                    .build());
        }
    }

    private void sendLocalStatistic(Statistic s)
    {
        if(CoordURI.equals("offline"))
        {
            tmp_buffer.offer(s);
            return;
        }

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(CoordURI).usePlaintext(true).build();
        NodeGRPCGrpc.NodeGRPCBlockingStub stub = NodeGRPCGrpc.newBlockingStub(channel);

        try
        {
            // salvo la stat lastGlobalStat
            lastGlobalStat = stub.sendStatistic(s);
            if(lastGlobalStat.getTimestamp() != 0)
                System.out.println(this.getId() + " - lastGlobalStat: " + String.format("%.14f",lastGlobalStat.getValue())
                        + " at "+ lastGlobalStat.getTimestamp());
            else
                System.out.println(this.getId() + " - No lastGlobalStat available yet");
        }
        catch(StatusRuntimeException e)
        {
            CoordURI = "offline";
            tmp_buffer.offer(s);
            System.out.println("Coordinator offline - starting new election...");
            newElection();
        }
        channel.shutdown();
    }

    private void sendBufferedStats()
    {
        PriorityQueue<Statistic> tmp_bufferCopy = new PriorityQueue<>(tmp_buffer);
        tmp_buffer.clear();
        tmp_bufferCopy.forEach(this::sendLocalStatistic);
    }

    // per l'elezione uso l'algoritmo di Bully
    synchronized void newElection()
    {
//      DEBUG - test entrata nodo ad elezione in corso
//
//        System.out.println("**** DEBUG - NEW ELECTION SLEEP FOR 5 SEC ****");
//        try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
//        System.out.println("**** DEBUG - NEW ELECTION AWAKED ****");

        if(isCoordinator)
            return;

        // filtro la mappa dei nodi conosciuti ottenendo quelli con ID maggiore al mio
        Map<String,String> eligible = localNodesList.entrySet()
                .stream()
                .filter(map -> map.getKey().compareTo(this.id) > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // se la mappa è vuota setto il nodo this come coordinatore
        // e comunico in broadcast la mia elezione
        if(eligible.size() == 0)
        {
            setMeAsCoordinator();
            localNodesList.keySet().forEach(v -> reportEndElection(v, localNodesList.get(v)));
            return;
        }
        // per ogni nodo trovato lancio l'rpc di elezione
        eligible.keySet().forEach(v -> electionGrpc(v, eligible.get(v)));

        // aspetto gli OK di risposta. Se non ne ricevo allora tutti i nodi
        // con id più alto sono usciti dalla rete e quindi il coordinatore sono io
        waitForOKs();
    }

    private synchronized void waitForOKs()
    {
        // aspetto gli OK in modo asincrono su un altro thread
        // in questo modo nel frattempo posso continuare a raccogliere statistiche
        if(waitOKs == null)
        {
            waitOKs = new WaitForOKs(this);
            new Thread(waitOKs).start();
        }
    }

    void setMeAsCoordinator()
    {
        if(isCoordinator)
            return;

        this.setIsCoordinator();
        this.CoordURI = this.nodeURI+":"+this.nodesPort;

        this.coordinatorThread = new CoordinatorThread(this);
        new Thread(this.coordinatorThread).start();

        System.out.println(this.getId() + " - I am the new coordinator");

        if(tmp_buffer.size() > 0)
            sendBufferedStats();
    }

    void setCoordinator(String coordId, String coordUri)
    {
        if(isCoordinator)
        {
            isCoordinator = false;
            coordinatorThread.stop();
            coordinatorThread = null;
        }

        this.waitOKs = null;
        this.CoordURI = coordUri;
        System.out.println(this.getId() + " - My new coordinator is "+coordId);
        if(tmp_buffer.size() > 0)
            sendBufferedStats();
    }

    private synchronized void electionGrpc(String id, String uri)
    {
        new Thread(new ParallelGrpcNewElection(id, uri, this)).start();
    }

    private void reportEndElection(String id, String uri)
    {
        new Thread(new ParallelGrpcReportCoord(id, uri,this)).start();
    }

    // quando un nodo entra in rete si presenta e chiede chi è il coordinatore
    private void reportToEdgeNetwork(HashSet<EdgeNode> nodeList)
    {
        int size = nodeList.size();

        // se la lista dei nodi ricevuta è vuota setto il nodo this come coordinatore
        if(size == 0)
        {
            this.setIsCoordinator();
            this.CoordURI = this.nodeURI+":"+this.nodesPort;
            this.coordinatorThread = new CoordinatorThread(this);
            new Thread(this.coordinatorThread).start();
            System.out.println(this.getId() + " - I am the coordinator");
            return;
        }

        System.out.println(this.getId() + " - Retrieving coordinator...");

        // lancio parallelo di gRPC - mi presento e chiedo chi è il coordinatore
        int i = 0;
        for(EdgeNode node: nodeList)
        {
            System.out.println("    - gRPC call " + (++i) + ": Asking to node "+ node.getId() + "...");
            new Thread(new ParallelGrpcCoordFinder(this, node.getId(),node.nodeURI+":"+node.getNodesPort(), i, size)).start();
        }

        // aspetto che tutte le chiamate gRPC ritornino
        try
        {
            synchronized(this)
            {
                wait();
            }
        }
        catch (InterruptedException e) { e.printStackTrace(); }

        // stampo il coordinatore ricevuto
        System.out.println(this.getId() + " - My coordinator is "+CoordURI);
    }

    synchronized void addNodeToLocalList(String id, String uri)
    {
        localNodesList.put(id, uri);
    }

    synchronized void removeNodeFromLocalList(String id)
    {
        localNodesList.remove(id);
    }

}