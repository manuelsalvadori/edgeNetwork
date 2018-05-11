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
        // TO DO
        return NodesGrid.getInstance().getEdgeNodeList().iterator().next();
    }
}
