package cloud_services;

import com.google.gson.Gson;
import edge_nodes.NodeIdentifier;
import edge_nodes.NodesGrid;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("NodeInit")
public class NodeService
{
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response getClosestNode(String nodeJson)
    {
        System.out.println(nodeJson);
        Gson g = new Gson();
        NodeIdentifier node = g.fromJson(nodeJson, NodeIdentifier.class);
        System.out.println(node);
        if(testLegality(node))
        {
            // restituire lista con o senza il nodo stesso?
            NodesGrid.getInstance().addNode(node);
            System.out.println(NodesGrid.getInstance().getNodeIdentifierList().iterator().next());
            return Response.ok(g.toJson(NodesGrid.getInstance().getNodeIdentifierList())).build();
        }
        else
            return Response.status(403).entity("Illegal position").build();
    }

    private boolean testLegality(NodeIdentifier node)
    {
        // TO DO
        return true;
    }
}
