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
        Gson g = new Gson();
        NodeIdentifier node = g.fromJson(nodeJson, NodeIdentifier.class);

        String isLegal = testLegality(node);

        if(isLegal.equals("Legal"))
        {
            String jsonNodeList = g.toJson(NodesGrid.getInstance().getNodeIdentifierList());
            NodesGrid.getInstance().addNode(node);
            return Response.ok(jsonNodeList).build();
        }

        return Response.status(403).entity(isLegal).build();
    }

    //rivedere
    private String testLegality(NodeIdentifier node)
    {
        if(!testPosition(node.getX(), node.getY()))
            return "Illegal position";

        if(!testNodeID(node.getId()))
            return "Illegal id";

        if(!testSensorsPort(node.getSensorsPort()))
            return "Illegal sensor port";

        if(!testNodesPort(node.getNodesPort()))
            return "Illegal node port";

        return "Legal";
    }

    private boolean testPosition(int x, int y)
    {
        for(NodeIdentifier nid: NodesGrid.getInstance().getNodeIdentifierList())
            if(!checkDistance(nid.getX(), nid.getY(), x, y))
                return false;
        return true;
    }

    private boolean testNodeID(String id)
    {
        for(NodeIdentifier nid: NodesGrid.getInstance().getNodeIdentifierList())
            if(nid.getId().equals(id))
                return false;
        return true;
    }

    private boolean testSensorsPort(int port)
    {
        for(NodeIdentifier nid: NodesGrid.getInstance().getNodeIdentifierList())
            if(nid.getSensorsPort() == port)
                return false;
        return true;
    }

    private boolean testNodesPort(int port)
    {
        for(NodeIdentifier nid: NodesGrid.getInstance().getNodeIdentifierList())
            if(nid.getNodesPort() == port)
                return false;
        return true;
    }

    private boolean checkDistance(int x1, int y1, int x2, int y2)
    {
        return Math.abs(x1-x2) + Math.abs(y1-y2) < 20;
    }
}
