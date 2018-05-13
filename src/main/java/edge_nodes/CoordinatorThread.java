package edge_nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import edge_nodes.NodeGRPCOuterClass.Statistic;

public class CoordinatorThread implements Runnable
{
    private EdgeNode node;
    private HashMap<String,PriorityQueue<Statistic>> statsBuffer;
    private volatile List<Statistic> lastLocalStats;
    private volatile Statistic lastGlobalStat;

    public CoordinatorThread(EdgeNode node)
    {
        this.node = node;
        lastGlobalStat = Statistic.newBuilder().setNodeID("Coord").setValue(0.0).setTimestamp(0).build();
    }

    @Override
    public void run()
    {
        statsBuffer = new HashMap<>();
        new Thread(new CoordinatorSender(this)).start();
    }

    public synchronized Statistic addStatistic(Statistic s)
    {
        if(!statsBuffer.containsKey(s.getNodeID()))
            statsBuffer.put(s.getNodeID(),new PriorityQueue<Statistic>(20,
                    (Statistic s1, Statistic s2) -> { return Long.compare(s1.getTimestamp(),s2.getTimestamp()); }));

        statsBuffer.get(s.getNodeID()).offer(s);
        return lastGlobalStat;
    }

    public synchronized HashMap<String, PriorityQueue<Statistic>> getStatsBuffer()
    {
        return new HashMap<>(statsBuffer);
    }

    public synchronized void clearStats()
    {
        statsBuffer.clear();
    }

    public List<Statistic> computeStats()
    {
        List<Statistic> ls = new ArrayList<>();
        HashMap<String, PriorityQueue<Statistic>> buffer = getStatsBuffer();

        if(buffer.size() == 0)
            return null;

        for(String nodeId: buffer.keySet())
        {
            double value = 0.0;
            for(Statistic s: buffer.get(nodeId))
            {
                value += s.getValue();
            }
            value /= buffer.get(nodeId).size();

            ls.add(Statistic.newBuilder().setNodeID(nodeId).setValue(value).setTimestamp(node.computeTimestamp()).build());
        }

        lastLocalStats = ls;

        double value = 0;
        for(Statistic s: ls)
        {
            value += s.getValue();
        }
        value /= ls.size();

        Statistic s = Statistic.newBuilder().setNodeID("Coord").setValue(value).setTimestamp(node.computeTimestamp()).build();
        lastGlobalStat = s;
        ls.add(s);

        return ls;
    }

    public EdgeNode getNode()
    {
        return node;
    }
}
