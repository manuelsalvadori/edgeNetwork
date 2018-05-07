package sensor;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import edge_nodes.Node;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

public class Sensor
{
    public static void main(String[] args)
    {
        try {

            ClientConfig config = new DefaultClientConfig();
            config.getClasses().add(JacksonJaxbJsonProvider.class);
            config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

            Client client = Client.create(config);

            WebResource webResource = client.resource("http://localhost:2018/SensorInit/");

            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            if (response.getStatus() != 200)
            {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            Node output = response.getEntity(Node.class);

            System.out.println("Output from Server .... \n");
            System.out.println(output.getId());

        } catch (Exception e) { e.printStackTrace(); }

    }
}
