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
import java.util.Map;
import java.util.PriorityQueue;
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
                .stream().map(node -> formatNodes(node)).collect(Collectors.toList());

        l.add(0, "Edge network has "+l.size()+" node");
        System.out.println(l); //debug
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
        List<String> l = null;
        try
        {
            l = CityStatistics.getInstance().getStats().get(nodeId)
                    .stream().limit(n).map(s -> formatStats(s)).collect(Collectors.toList());
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

        int lenght = 0;
        for(String id: CityStatistics.getInstance().getStats().keySet())
        {
            try
            {
                l = Stream.concat(l.stream(),CityStatistics.getInstance().getStats().get(id)
                        .stream().limit(n).map(s -> formatStats(s))).collect(Collectors.toList());
            }
            catch (NullPointerException npe)
            {
                return Response.status(404).build();
            }
            // la stringa head non è giusta

            if (l.size() - lenght < n)
                l.add(lenght, id + " stats - There are available only " + (l.size() - lenght) + " statistics:");
            else
                l.add(lenght, id + " stats - Last " + n + " statistics:");
            lenght = l.size();
        }
        return Response.ok(new Gson().toJson(l)).build();
    }

    @GET
    @Path("node/sd/{id}/{n}")
    @Produces("application/json")
    public Response getNodeStandardDeviation(@PathParam("id") String id, @PathParam("n") int n)
    {
        double[] l = null;
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

        String resp = "";
        if(l.length < n)
            resp = id + " - Standard deviation: " + sd + " - (available only " + l.length + " statistics)";
        else
            resp = id + " - Standard deviation: " + sd + ", Mean: "+mean;

        return Response.ok(new Gson().toJson(l)).build();
    }

    private double toValue(Statistic s)
    {
        return s.getValue();
    }
}
