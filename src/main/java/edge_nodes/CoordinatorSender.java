package edge_nodes;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import java.util.List;
import java.util.TimerTask;

public class CoordinatorSender implements Runnable
{
    private CoordinatorThread coordinator;
    private Client RESTclient;

    CoordinatorSender(CoordinatorThread coordinator)
    {
        this.coordinator = coordinator;
        this.RESTclient = restClientInit();
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) { e.printStackTrace(); }

            sendStatsToServer();
        }
    }

    private void sendStatsToServer()
    {
        List<NodeGRPCOuterClass.Statistic> l = coordinator.computeStats();
        if(l == null)
            return;

        ClientResponse response;
        try
        {
            String serverURI = coordinator.getNode().getServerURI();
            WebResource webResource = RESTclient.resource(serverURI +"/SendStatistics/");

            response = webResource.type("application/json").post(ClientResponse.class, new Gson().toJson(l));
        }
        catch(ClientHandlerException ce)
        {
            System.out.println("COORDINATOR - Server cloud connection refused - impossible to send data");
            return;
        }

        switch (response.getStatus())
        {
            case 200:
                System.out.println("COORDINATOR - Sending statistics to server successful" );
                break;

            default:
                System.out.println("COORDINATOR - Failed sending statistics: HTTP error code: " + response.getStatus());
        }
    }

    private Client restClientInit()
    {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        System.out.println("COORDINATOR - REST client configurated");
        return Client.create(config);
    }
}
