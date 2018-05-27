package server_containers;

import edge_nodes.NodeGRPCOuterClass.Statistic;

import java.util.*;

public class CityStatistics
{
    private static CityStatistics instance;
    private HashMap<String, PriorityQueue<Statistic>> stats;
    private final int bound = 200;

    private CityStatistics()
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
        HashMap<String, PriorityQueue<Statistic>> copy = new HashMap<>();
        stats.keySet().forEach(k -> copy.put(k,new PriorityQueue<>(stats.get(k))));
        return copy;
    }

    public synchronized void addStats(List<Statistic> l)
    {
        for(Statistic s: l)
        {
            if(!stats.containsKey(s.getNodeID()))
                stats.put(s.getNodeID(), new PriorityQueue<>(bound, Comparator.comparingLong(Statistic::getTimestamp)));

            if(stats.get(s.getNodeID()).size() >= bound)
                discardStat(s.getNodeID());

            stats.get(s.getNodeID()).offer(s);
        }
    }

    private synchronized void discardStat(String node)
    {
        stats.get(node).poll();
    }
}
