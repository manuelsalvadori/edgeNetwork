package cloud_services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edge_nodes.NodeGRPCOuterClass.Statistic;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("SendStatistics")
public class CoordinatorService
{
    @POST
    @Consumes("application/json")
    public Response addStats(String nodeJson)
    {
        Gson g = new Gson();
        List<Statistic> stats = g.fromJson(nodeJson, new TypeToken<List<Statistic>>(){}.getType());

        try
        {
            CityStatistics.getInstance().addStats(stats);
            System.out.println(CityStatistics.getInstance().getStats()); //debug
            return Response.ok().build();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return Response.status(400).build();
        }

    }
}
