package cloud_services;

import com.google.gson.Gson;
import edge_nodes.EdgeNode;
import server_containers.NodesGrid;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
@Path("Node")
public class NodeService
{
    @Path("NodeInit")
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response getClosestNode(String nodeJson)
    {
        Gson g = new Gson();
        EdgeNode node = g.fromJson(nodeJson, EdgeNode.class);

        String isLegal = testLegality(node);

        if(isLegal.equals("Legal"))
        {
            String jsonNodeList = g.toJson(NodesGrid.getInstance().getEdgeNodeList());
            NodesGrid.getInstance().addNode(node);
            return Response.ok(jsonNodeList).build();
        }

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
        if(!checkDistance(nid.getX(), nid.getY(), x, y))
            return false;
        return true;
    }

    private boolean testNodeID(String id, EdgeNode nid)
    {
        if(nid.getId().equals(id))
            return false;
        return true;
    }

    private boolean testSensorsPort(int port, EdgeNode nid)
    {
        if(nid.getSensorsPort() == port)
            return false;
        return true;
    }

    private boolean testNodesPort(int port, EdgeNode nid)
    {
        if(nid.getNodesPort() == port)
            return false;
        return true;
    }

    private boolean checkDistance(int x1, int y1, int x2, int y2)
    {
        return Math.abs(x1-x2) + Math.abs(y1-y2) > 20;
    }

    @Path("RemoveNode/{id}")
    @DELETE
    public Response removeNode(@PathParam("id") String id)
    {
        if(NodesGrid.getInstance().removeNode(id))
            return Response.ok().build();

        return Response.status(404).entity("No such node in the cloud").build();
    }
}
