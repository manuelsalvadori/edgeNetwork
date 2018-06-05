package sensor;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import edge_nodes.EdgeNode;

public class SensorUpdate implements Runnable
{
    private final ActualSensorStream ss;

    SensorUpdate(ActualSensorStream ss)
    {
        this.ss = ss;
    }

    @Override
    public void run()
    {
        System.out.println(ss.getId()+ " - SensorUpdate started");
        while(true)
        {
            try
            {
                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            {
                System.out.println(ss.getId()+" - Connection with node lost - Retrieving new node in advance...");
            }

            retrieveNode();
        }

    }

    private void retrieveNode()
    {
        System.out.println(ss.getId()+" - Retrieving node...");

        ClientResponse response;
        try
        {
            WebResource webResource = ss.getSensorClient().resource(ss.getServerUri()+"/SensorInit/"+ss.getX()+"/"+ss.getY());
            response = webResource.accept("application/json").get(ClientResponse.class);
        }
        catch(ClientHandlerException ce)
        {
            System.out.println(ss.getId()+" - Server cloud connection refused - impossible to retrieve a node");
            ss.setMyEdgeNode(null);
            return;
        }

        EdgeNode output = null;

        switch (response.getStatus())
        {
            case 200:
                String json = response.getEntity(String.class);
                output = new Gson().fromJson(json, EdgeNode.class);
                System.out.println(ss.getId()+" - Received new node; ID: " + output.getId());
                break;

            case 204:
                System.out.println(ss.getId()+" - No node available");
                break;

            default:
                System.out.println(ss.getId()+" - Failed sensor init: HTTP error code: " + response.getStatus());
        }
        ss.setMyEdgeNode(output);
    }
}
