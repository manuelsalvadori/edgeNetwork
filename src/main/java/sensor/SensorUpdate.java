package sensor;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import edge_nodes.Node;

public class SensorUpdate implements Runnable
{
    ActualSensorStream ss;


    public SensorUpdate(ActualSensorStream ss)
    {
        this.ss = ss;
    }

    @Override
    public void run()
    {
        System.out.println("SensorUpdate started");
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
            ss.setMyNode(null);
            return;
        }

        Node output = null;

        switch (response.getStatus())
        {
            case 200:
                output = response.getEntity(Node.class);
                System.out.println(ss.getId()+" - Received new node; ID: " + output.getId());
                break;

            case 404:
                System.out.println(ss.getId()+" - No node available");
                break;

            default:
                System.out.println(ss.getId()+" - Failed sensor init: HTTP error code: " + response.getStatus());
        }

        ss.setMyNode(output);
    }
}
