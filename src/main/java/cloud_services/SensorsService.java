package cloud_services;

import com.google.gson.Gson;
import edge_nodes.EdgeNode;
import edge_nodes.NodesGrid;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("SensorInit/{x}/{y}")
public class SensorsService
{
    @GET
    @Produces("application/json")
    public Response getClosestNode(@PathParam("x") int x, @PathParam("y") int y)
    {
        System.out.println("x:"+x+" y:"+y);
        EdgeNode nid = retrieveNode(x,y);

        if(nid != null)
            return Response.ok(new Gson().toJson(nid)).build();
        return Response.status(404).entity("No node available").build();
    }

    private EdgeNode retrieveNode(int x, int y)
    {
        EdgeNode node = null;
        int minDistance = Integer.MAX_VALUE;
        for(EdgeNode n: NodesGrid.getInstance().getEdgeNodeList())
        {
            int dist = distance(x, y, n.getX(), n.getY());
            if(dist < minDistance)
            {
                minDistance = dist;
                node = n;
            }
        }
        return node;
    }

    private int distance(int x1, int y1, int x2, int y2)
    {
        return Math.abs(x1-x2) + Math.abs(y1-y2);
    }
}
