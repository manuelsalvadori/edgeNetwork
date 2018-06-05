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

import java.util.ArrayList;
import java.util.List;
import edge_nodes.NodeGRPCOuterClass.Statistic;

public class CoordinatorSender implements Runnable // questo thread si occupa di inviare ogni 5 sec le stats al server
{
    private final CoordinatorThread coordinator;
    private final Client RESTclient;
    private List<List<Statistic>> backupBuffer;

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

            System.out.println("-----------------------------------------------------------------------");

            sendStatsToServer(coordinator.computeStats());

            if(backupBuffer.size() > 0)
            {
                List<List<Statistic>> copyList = copyList(backupBuffer);
                backupBuffer.clear();

                for (List<Statistic> l: copyList)
                {
                    long timestamp = l.stream().filter((Statistic s) -> s.getNodeID().equals("Coord"))
                            .mapToLong(Statistic::getTimestamp).toArray()[0];

                    System.out.println("COORDINATOR - Sending buffered stats computed at " + timestamp);
                    sendStatsToServer(l);
                }
            }
            System.out.println("-----------------------------------------------------------------------");
        }
    }

    // quando invio le stats, se il server non risponde le salvo in un buffer di backup e riprovo fra 5 secondi
    private void sendStatsToServer(List<Statistic> stats)
    {
        if(stats == null)
        {
            System.out.println("COORDINATOR - Nothing to send yet");
            return;
        }

        ClientResponse response;
        String serverURI = coordinator.getNode().getServerURI();

        try
        {
            WebResource webResource = RESTclient.resource(serverURI + "/Statistics/");
            response = webResource.type("application/json").put(ClientResponse.class, new Gson().toJson(stats, new TypeToken<List<Statistic>>(){}.getType()));
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

    private List<List<Statistic>> copyList(List<List<Statistic>> toCopy)
    {
        List<List<Statistic>> copy = new ArrayList<>(toCopy.size());
        toCopy.forEach(e -> copy.add(new ArrayList<>(e)));
        return copy;
    }
}
