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

import java.util.ArrayList;
import java.util.List;

public class CoordinatorSender implements Runnable
{
    private CoordinatorThread coordinator;
    private Client RESTclient;
    private List<List<NodeGRPCOuterClass.Statistic>> backupBuffer;

    CoordinatorSender(CoordinatorThread coordinator)
    {
        this.coordinator = coordinator;
        this.RESTclient = restClientInit();
        this.backupBuffer = new ArrayList<>();
    }

    @Override
    public void run()
    {
        // ogni 5 secondi invio le statistiche al server
        while(true)
        {
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) { e.printStackTrace(); }

            sendStatsToServer(coordinator.computeStats());

            if(backupBuffer.size() > 0)
            {
                backupBuffer.forEach(this::sendStatsToServer);
                backupBuffer.clear();
            }
        }
    }

    // se il server non risponde salvo le stats in un buffer e riprovo fra 5 secondi
    private void sendStatsToServer(List<NodeGRPCOuterClass.Statistic> stats)
    {
        if(stats == null)
            return;

        ClientResponse response;
        String serverURI = coordinator.getNode().getServerURI();

        try
        {
            WebResource webResource = RESTclient.resource(serverURI + "/SendStatistics/");
            response = webResource.type("application/json").post(ClientResponse.class, new Gson().toJson(stats));
        }
        catch (ClientHandlerException ce)
        {
            System.out.println("COORDINATOR - Server cloud connection refused - impossible to send data");
            backupBuffer.add(stats);
            return;
        }

        switch (response.getStatus())
        {
            case 200:
                System.out.println("COORDINATOR - Sending statistics to server successful");
                return;

            default:
                backupBuffer.add(stats);
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
