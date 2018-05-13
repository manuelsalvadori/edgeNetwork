package server_containers;

import edge_nodes.NodeGRPCOuterClass.Statistic;

import java.util.*;

public class CityStatistics
{
    private static CityStatistics instance;
    private HashMap<String, PriorityQueue<Statistic>> stats;
    final int bound = 100;

    public CityStatistics()
    {
        this.stats = new HashMap<>();
    }

    public synchronized static CityStatistics getInstance()
    {
        if(instance == null)
            instance = new CityStatistics();
        return instance;
    }

    public synchronized Map<String, PriorityQueue<Statistic>> getStats()
    {
        return new HashMap<>(stats);
    }

    public synchronized void addStats(List<Statistic> l)
    {
        for(Statistic s: l)
        {
            if(!stats.containsKey(s.getNodeID()))
                stats.put(s.getNodeID(),new PriorityQueue<Statistic>(100,(Statistic s1, Statistic s2) -> { return Long.compare(s1.getTimestamp(),s2.getTimestamp()); }));
            if(stats.get(s.getNodeID()).size() >= bound)
                discardStat(s.getNodeID());
            stats.get(s.getNodeID()).offer(s);
        }
    }

    public synchronized void discardStat(String node)
    {
        stats.get(node).poll();
    }


}
