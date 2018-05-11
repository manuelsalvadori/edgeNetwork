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
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;


public class Node
{

    private NodeIdentifier nodeIdentifier;
    private String serverURI;

    public Node(String id, String serverURI, int sensorPort, int nodePort)
    {
        this.nodeIdentifier = new NodeIdentifier(id, "localhost", sensorPort, nodePort);
        this.serverURI = serverURI;
    }

    public String getId()
    {
        return nodeIdentifier.getId();
    }

    public String getNodeURI()
    {
        return nodeIdentifier.getNodeURI();
    }

    public int getSensorsPort()
    {
        return nodeIdentifier.getSensorsPort();
    }

    public int getNodesPort()
    {
        return nodeIdentifier.getNodesPort();
    }

    public String getServerURI()
    {
        return serverURI;
    }

    public static void main(String[] args )
    {
        Scanner io = new Scanner(System.in);
        System.out.println("Node Initialization...");
        System.out.println("Insert sensors port:");
        int sensorPort = io.nextInt();
        System.out.println("Insert nodes port:");
        int nodePort = io.nextInt();
        io.nextLine();
        System.out.println("Insert server address:");
        String serverURI = io.nextLine();
        System.out.println("Insert nodes id:");
        String nodeId = io.nextLine();

        Node node = new Node(nodeId, serverURI, sensorPort, nodePort);
        node.nodeInit(2,3);

        try
        {
            Server server = ServerBuilder.forPort(sensorPort).addService(new SensorGRPCImpl()).build();
            server.start();
            System.out.println("Node started!");
            server.awaitTermination();
        }
        catch (IOException e) {e.printStackTrace();}
        catch (InterruptedException e) {e.printStackTrace();}


    }

    private HashSet<NodeIdentifier> nodeInit(int x, int y)
    {
        System.out.println(this.getId()+ " - Node initialization...");
        ClientResponse response;
        try
        {
            WebResource webResource = restClientInit().resource(this.serverURI+"/NodeInit/");
            String json = new Gson().toJson(this.nodeIdentifier);
            response = webResource.type("application/json").post(ClientResponse.class, json);
        }
        catch(ClientHandlerException ce)
        {
            System.out.println(this.getId()+ " - Server cloud connection refused - init impossible");
            return null;
        }

        HashSet<NodeIdentifier> nodeList = null;

        switch (response.getStatus())
        {
            case 200:
                String json = response.getEntity(String.class);
                nodeList = new Gson().fromJson(json, new TypeToken<HashSet<NodeIdentifier>>(){}.getType());
                System.out.println(this.getId()+ " - Successfully added to the cloud");
                break;

            case 403:
                System.out.println(this.getId()+ " - Illegal position");
                break;

            default:
                System.out.println(this.getId()+ " - Failed node init: HTTP error code: " + response.getStatus());
        }

        return nodeList;
    }

    private Client restClientInit()
    {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        System.out.println(this.nodeIdentifier.getId()+ " - Client configurated");
        return Client.create(config);
    }
}
