package sensor;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import edge_nodes.Node;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import simulation.Measurement;
import simulation.PM10Simulator;
import simulation.SensorStream;

import java.util.Random;

public class Sensor
{
    private PM10Simulator pm10sim;
    private Client sensorClient;
    private Node myNode;
    private String serverUri = "http://localhost:2018";

    public Sensor(String restServerUri)
    {
        this.serverUri = restServerUri;
    }

    public void runSensor()
    {
        try
        {

            pm10sim = new PM10Simulator(new ActualSensorStream());
            sensorClient = restClientInit();
            myNode = sensorInit(serverUri, getCoord(), getCoord());

        }
        catch (Exception e) {e.printStackTrace();}

    }

    private Node sensorInit(String uri, int x, int y)
    {
        WebResource webResource = sensorClient.resource(serverUri+"/SensorInit/"+x+"/"+y);
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

        if (response.getStatus() != 200)
            throw new RuntimeException("Failed sensor init: HTTP error code: " + response.getStatus());

        Node output = response.getEntity(Node.class);
        System.out.print("Received Node id: "+output.getId());

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

    private int getCoord()
    {
        Random rnd = new Random(System.currentTimeMillis());
        return rnd.nextInt(99);
    }

}
