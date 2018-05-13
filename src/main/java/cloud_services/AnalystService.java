package cloud_services;

import com.google.gson.Gson;
import edge_nodes.EdgeNode;
import server_containers.CityStatistics;
import server_containers.NodesGrid;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("getStatistics")
public class AnalystService
{
    @GET
    @Path("cityState")
    @Produces("application/json")
    public Response getCityState()
    {
        List<String> l = new ArrayList<>();
        for(EdgeNode node: NodesGrid.getInstance().getEdgeNodeList())
        {
            l.add(" - Edge node "+node.getId() + " at position: ("+node.getX()+","+node.getY()+")");
        }

        l.add(0, "Edge network has "+l.size()+" node");
        System.out.println(l);
        return Response.ok(new Gson().toJson(l)).build();
    }
}
