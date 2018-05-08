package sensor;

import com.sun.jersey.api.client.Client;
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
            catch (InterruptedException e) { e.printStackTrace(); }

            retrieveNode();

        }

    }

    private void retrieveNode()
    {
        System.out.println("Retrieving node...");
        WebResource webResource = ss.getSensorClient().resource(ss.getServerUri()+"/SensorInit/"+ss.getX()+"/"+ss.getY());
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

        if (response.getStatus() != 404 && response.getStatus() != 200)
            throw new RuntimeException("Failed retrieving node: HTTP error code: " + response.getStatus());

        Node output = null;
        if(response.getStatus() != 404)
        {
            output = response.getEntity(Node.class);
            System.out.println("Received new node; ID: " + output.getId());
        }
        else
            System.out.println("No node available");

        ss.setMyNode(output);
    }
}
