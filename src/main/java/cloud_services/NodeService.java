package cloud_services;

import com.google.gson.Gson;
import edge_nodes.EdgeNode;
import server_containers.NodesGrid;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("Node")
public class NodeService
{
    @Path("{id}")
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response addNewNode(String nodeJson, @PathParam("id") String id)
    {
        System.out.println("Node " + id + " requesting add to network...");
        Gson g = new Gson();
        EdgeNode node = g.fromJson(nodeJson, EdgeNode.class);

        String isLegal = testLegality(node);

        if(isLegal.equals("Legal"))
        {
            String jsonNodeList = g.toJson(NodesGrid.getInstance().getEdgeNodeList());
            NodesGrid.getInstance().addNode(node);
            return Response.ok(jsonNodeList).build();
        }
        else
        if(isLegal.equals("Illegal position"))
            return Response.status(403).entity(isLegal).build();

        return Response.status(400).entity(isLegal).build();
    }

    private String testLegality(EdgeNode node)
    {
        if(node.getSensorsPort() == node.getNodesPort())
            return "Illegal ports: they must be unique";

        for(EdgeNode nid: NodesGrid.getInstance().getEdgeNodeList())
        {
            if (!testNodeID(node.getId(), nid))
                return "Illegal id";

            if (!testSensorsPort(node.getSensorsPort(), nid))
                return "Illegal sensor port";

            if (!testNodesPort(node.getNodesPort(), nid))
                return "Illegal node port";

            if (!testPosition(node.getX(), node.getY(), nid))
                return "Illegal position";
        }
        return "Legal";
    }

    private boolean testPosition(int x, int y, EdgeNode nid)
    {
        return checkDistance(nid.getX(), nid.getY(), x, y);
    }

    private boolean testNodeID(String id, EdgeNode nid)
    {
        return !nid.getId().equals(id);
    }

    private boolean testSensorsPort(int port, EdgeNode nid)
    {
        return nid.getSensorsPort() != port;
    }

    private boolean testNodesPort(int port, EdgeNode nid)
    {
        return nid.getNodesPort() != port;
    }

    private boolean checkDistance(int x1, int y1, int x2, int y2)
    {
        return Math.abs(x1-x2) + Math.abs(y1-y2) > 20;
    }

    @Path("{id}")
    @DELETE
    public Response removeNode(@PathParam("id") String id)
    {
        if(NodesGrid.getInstance().removeNode(id))
            return Response.ok().build();

        return Response.status(404).entity("No such node in the cloud").build();
    }
}
