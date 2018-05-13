package edge_nodes;

import java.util.Comparator;
import java.util.PriorityQueue;
import edge_nodes.NodeGRPCOuterClass.Statistic;

public class CoordinatorThread implements Runnable
{
    private EdgeNode node;
    private PriorityQueue<Statistic> queue;
    private Statistic lastGlobalStat;

    @Override
    public void run()
    {
        queue = new PriorityQueue<Statistic>(20,(Statistic s1, Statistic s2) -> { return Long.compare(s1.getTimestamp(),s2.getTimestamp()); });
    }

    public synchronized Statistic addStatistic(Statistic s)
    {
        queue.offer(s);
        System.out.println(queue);
        return Statistic.newBuilder().setNodeID("Coord").setValue(10.0).setTimestamp(1000).build();
    }

}
