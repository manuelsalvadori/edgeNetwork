package cloud_services;

import edge_nodes.Node;

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
        if(System.currentTimeMillis()%2==0)
            return Response.ok(new Node("Node1", "localhost",4000)).build();
        else
            return Response.ok(new Node("Node2", "localhost",4001)).build();
    }
}
