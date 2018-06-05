package sensor;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.client.ClientHandlerException;
import edge_nodes.EdgeNode;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import simulation.PM10Simulator;

import java.util.Random;

public class SensorInitializer
{
    private Client sensorClient;
    private final String serverUri;

    SensorInitializer(String restServerUri)
    {
        this.serverUri = restServerUri;
    }

    // creo e faccio partire il thread simulatore di sensore
    public void runSensor(String id)
    {
        try
        {
            sensorClient = restClientInit(id);
            int[] coord = getCoord();
            EdgeNode myEdgeNode = sensorInit(id, coord[0], coord[1]);
            PM10Simulator pm10 = new PM10Simulator(new ActualSensorStream(id, sensorClient, serverUri, myEdgeNode, coord[0], coord[1]));
            pm10.start();
        }
        catch (Exception e) {e.printStackTrace();}

    }

    // mando la richiesta REST al server per ottenere il nodo assegnato
    private EdgeNode sensorInit(String id, int x, int y)
    {
        System.out.println(id+ " - Sensor initialization...");
        ClientResponse response;
        try
        {
            WebResource webResource = sensorClient.resource(serverUri+"/SensorInit/"+x+"/"+y);
            response = webResource.accept("application/json").get(ClientResponse.class);
        }
        catch(ClientHandlerException ce)
        {
            System.out.println(id+ " - Server cloud connection refused - impossible to retrieve a node");
            return null;
        }

        EdgeNode output = null;

        switch (response.getStatus())
        {
            case 200:
                String json = response.getEntity(String.class);
                output = new Gson().fromJson(json, EdgeNode.class);
                System.out.println(id+ " - Received node; ID: " + output.getId());
                break;

            case 404:
                System.out.println(id+ " - No node available");
                break;

            default:
                System.out.println(id+ " - Failed sensor init: HTTP error code: " + response.getStatus());
        }

        return output;
    }

    private Client restClientInit(String id)
    {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        System.out.println(id+ " - Client configurated");
        return Client.create(config);
    }

    private static int[] getCoord()
    {
        Random rnd = new Random(System.currentTimeMillis());
        return new int[]{rnd.nextInt(99), rnd.nextInt(99)};
    }

}
