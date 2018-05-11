package cloud_services;

import com.google.gson.Gson;
import edge_nodes.EdgeNode;
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
        Gson g = new Gson();
        EdgeNode node = g.fromJson(nodeJson, EdgeNode.class);

        String isLegal = testLegality(node);

        if(isLegal.equals("Legal"))
        {
            String jsonNodeList = g.toJson(NodesGrid.getInstance().getEdgeNodeList());
            NodesGrid.getInstance().addNode(node);
            return Response.ok(jsonNodeList).build();
        }

        return Response.status(403).entity(isLegal).build();
    }

    private String testLegality(EdgeNode node)
    {
        for(EdgeNode nid: NodesGrid.getInstance().getEdgeNodeList())
        {
            if (!testPosition(node.getX(), node.getY(), nid))
                return "Illegal position";

            if (!testNodeID(node.getId(), nid))
                return "Illegal id";

            if (!testSensorsPort(node.getSensorsPort(), nid))
                return "Illegal sensor port";

            if (!testNodesPort(node.getNodesPort(), nid))
                return "Illegal node port";
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
}
