package sensor;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.client.ClientHandlerException;
import edge_nodes.Node;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import simulation.PM10Simulator;

import java.util.Random;

public class SensorInitializer
{
    private Client sensorClient;
    private String serverUri;

    public SensorInitializer(String restServerUri)
    {
        this.serverUri = restServerUri;
    }

    public void runSensor(String id)
    {
        try
        {
            sensorClient = restClientInit();
            int[] coord = getCoord();
            Node myNode = sensorInit(coord[0], coord[1]);
            PM10Simulator pm10 = new PM10Simulator(new ActualSensorStream(id, sensorClient, serverUri, myNode, coord[0], coord[1]));
            pm10.start();
        }
        catch (Exception e) {e.printStackTrace();}

    }

    private Node sensorInit(int x, int y)
    {
        System.out.println("Sensor initialization...");
        ClientResponse response;
        try
        {
            WebResource webResource = sensorClient.resource(serverUri+"/SensorInit/"+x+"/"+y);
            response = webResource.accept("application/json").get(ClientResponse.class);
        }
        catch(ClientHandlerException ce)
        {
            System.out.println("Server cloud connection refused - impossible to retrieve a node");
            return null;
        }

        Node output = null;

        switch (response.getStatus())
        {
            case 200:
                output = response.getEntity(Node.class);
                System.out.print("Received node; ID: " + output.getId());
                break;

            case 404:
                System.out.println("No node available");
                break;

            default:
                System.out.println("Failed sensor init: HTTP error code: " + response.getStatus());
        }

        return output;
    }

    private Client restClientInit()
    {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        System.out.println("Client configurated");
        return Client.create(config);
    }

    public static int[] getCoord()
    {
        Random rnd = new Random(System.currentTimeMillis());
        return new int[]{rnd.nextInt(99), rnd.nextInt(99)};
    }

}