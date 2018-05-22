package cloud_services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edge_nodes.NodeGRPCOuterClass.Statistic;
import server_containers.CityStatistics;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("SendStatistics")
public class CoordinatorService
{
    //static int count = 10; // debug
    @POST
    @Consumes("application/json")
    public Response addStats(String nodeJson)
    {
        Gson g = new Gson();
        List<Statistic> stats = g.fromJson(nodeJson, new TypeToken<List<Statistic>>(){}.getType());

        //if(count-- > 0) // debug per il testing del buffer del coordinatore
        //    return Response.status(503).build();
        try
        {
            CityStatistics.getInstance().addStats(stats);
            return Response.ok().build();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return Response.status(400).build();
        }

    }
}
