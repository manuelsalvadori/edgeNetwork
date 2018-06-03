package cloud_services;

import com.google.gson.Gson;
import edge_nodes.EdgeNode;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import server_containers.CityStatistics;
import server_containers.NodesGrid;
import edge_nodes.NodeGRPCOuterClass.Statistic;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("getStatistics")
public class AnalystService
{
    @GET
    @Path("cityState")
    @Produces("application/json")
    public Response getCityState()
    {
        List<String> l = NodesGrid.getInstance().getEdgeNodeList()
                .stream().map(this::formatNodes).collect(Collectors.toList());

        l.add(0, "Edge network has "+l.size()+" node");
        return Response.ok(new Gson().toJson(l)).build();
    }

    private String formatNodes(EdgeNode n)
    {
        return " - Edge node " + n.getId() + " at position: ("+n.getX() + "," + n.getY() + ")";
    }

    @GET
    @Path("node/{id}/{n}")
    @Produces("application/json")
    public Response getNodeStats(@PathParam("id") String nodeId, @PathParam("n") int n)
    {
        List<String> l;
        try
        {
            l = CityStatistics.getInstance().getStats().get(nodeId)
                    .stream().limit(n).map(this::formatStats).collect(Collectors.toList());
        }
        catch (NullPointerException npe)
        {
            return Response.status(404).build();
        }

        if(l.size() < n)
            l.add(0, "Node " + nodeId + " - There are available only " + l.size() + " statistics:");
        else
            l.add(0, "Node " + nodeId + " - Last " + n + " statistics:");

        return Response.ok(new Gson().toJson(l)).build();
    }

    private String formatStats(Statistic s)
    {
        return " - " + s.getValue() + " " + s.getTimestamp();
    }

    @GET
    @Path("node/global/{n}")
    @Produces("application/json")
    public Response getGlobalStats(@PathParam("n") int n)
    {
        List<String> l = new ArrayList<>();

        int length = 0;
        for(String id: CityStatistics.getInstance().getStats().keySet())
        {
            try
            {
                l = Stream.concat(l.stream(), CityStatistics.getInstance().getStats().get(id)
                        .stream().limit(n).map(this::formatStats)).collect(Collectors.toList());
            }
            catch (NullPointerException npe)
            {
                return Response.status(404).build();
            }

            if (l.size() - length < n)
                l.add(length, id + " stats - Available only " + (l.size() - length) + " statistics:");
            else
                l.add(length, id + " stats - Last " + n + " statistics:");
            length = l.size();
        }
        if(l.size() == 0)
            l.add("No statistics received yet - try later");
        return Response.ok(new Gson().toJson(l)).build();
    }

    @GET
    @Path("node/sd/{id}/{n}")
    @Produces("application/json")
    public Response getNodeStandardDeviation(@PathParam("id") String id, @PathParam("n") int n)
    {
        double[] l;
        try
        {
            l = CityStatistics.getInstance().getStats().get(id)
                    .stream().limit(n).map(Statistic::getValue).mapToDouble(Double::doubleValue).toArray();
        }
        catch (NullPointerException npe)
        {
            return Response.status(404).build();
        }

        double sd = new StandardDeviation().evaluate(l);
        double mean = new Mean().evaluate(l);

        String resp;
        if(l.length < n)
            resp = id + " - Standard deviation: " + sd + ", Mean: "+mean + " - (available only " + l.length + " statistics)";
        else
            resp = id + " - Standard deviation: " + sd + ", Mean: "+mean;

        return Response.ok(new Gson().toJson(resp)).build();
    }

    @GET
    @Path("node/globalsd/{n}")
    @Produces("application/json")
    public Response getGlobalStandardDeviation(@PathParam("n") int n)
    {
        List<Double> l = new ArrayList<>();

        for(String id: CityStatistics.getInstance().getStats().keySet())
        {
            try
            {
                l = Stream.concat(l.stream(), CityStatistics.getInstance().getStats().get(id)
                        .stream().limit(n).map(Statistic::getValue)).collect(Collectors.toList());
            }
            catch (NullPointerException npe)
            {
                return Response.status(404).build();
            }
        }

        if(l.size() == 0)
            return Response.ok(new Gson().toJson("No statistics available yet - Try later")).build();

        double[] array = l.stream().mapToDouble(Double::doubleValue).toArray();

        double sd = new StandardDeviation().evaluate(array);
        double mean = new Mean().evaluate(array);

        return Response.ok(new Gson().toJson("Global statistics - Standard deviation: " + sd + ", Mean: "+mean)).build();
    }
}
