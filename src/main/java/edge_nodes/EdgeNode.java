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
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import java.util.HashSet;

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
    private HashSet<EdgeNode> localNodesList = new HashSet<>();

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

    public void setIsCoordinator(boolean isCoordinator)
    {
        this.isCoordinator = isCoordinator;
    }

    public String getServerURI()
    {
        return serverURI;
    }

    public HashSet<EdgeNode> getLocalNodesList()
    {
        return localNodesList;
    }

    public void setLocalNodesList(HashSet<EdgeNode> localNodesList)
    {
        this.localNodesList = localNodesList;
    }

    public void nodeInit(int x, int y)
    {
        this.x = x;
        this.y = y;
        System.out.println(this.getId()+ " - Node initialization...");
        ClientResponse response;
        try
        {
            WebResource webResource = restClientInit().resource(this.serverURI+"/NodeInit/");
            String json = new Gson().toJson(this);
            response = webResource.type("application/json").post(ClientResponse.class, json);
        }
        catch(ClientHandlerException ce)
        {
            System.out.println(this.getId()+ " - Server cloud connection refused - init impossible");
            return;
        }

        switch (response.getStatus())
        {
            case 200:
                String json = response.getEntity(String.class);
                this.localNodesList = new Gson().fromJson(json, new TypeToken<HashSet<EdgeNode>>(){}.getType());
                System.out.println(this.getId() + " - Successfully added to the cloud");
                break;

            case 403:
                System.out.println(this.getId() + response.getEntity(String.class));
                break;

            default:
                System.out.println(this.getId() + " - Failed node init: HTTP error code: " + response.getStatus());
        }
    }

    private Client restClientInit()
    {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        System.out.println(this.getId()+ " - Client configurated");
        return Client.create(config);
    }

}