package services;

import edge_nodes.Node;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("SensorInit")
public class SensorsService
{
    @GET
    @Produces("application/json")
    public Response getClosestNode()
    {
        return Response.ok(new Node("Node")).build();
    }
}
