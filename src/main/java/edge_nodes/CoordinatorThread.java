package edge_nodes;

import java.util.*;

import edge_nodes.NodeGRPCOuterClass.Statistic;

public class CoordinatorThread implements Runnable
{
    private EdgeNode node;
    private HashMap<String,PriorityQueue<Statistic>> statsBuffer;
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

        // ogni 5 secondi invio le statistiche al server
        new Thread(new CoordinatorSender(this)).start();
    }

    public synchronized Statistic addStatistic(Statistic s)
    {
        if(!statsBuffer.containsKey(s.getNodeID()))
            statsBuffer.put(s.getNodeID(),new PriorityQueue<>(20, Comparator.comparingLong(Statistic::getTimestamp)));

        statsBuffer.get(s.getNodeID()).offer(s);
        return lastGlobalStat;
    }

    public synchronized HashMap<String, PriorityQueue<Statistic>> getStatsBuffer()
    {
        return new HashMap<>(statsBuffer);
    }

    public synchronized void clearStatsBuffer()
    {
        statsBuffer.clear();
    }

    public List<Statistic> computeStats()
    {
        List<Statistic> ls = new ArrayList<>();
        HashMap<String, PriorityQueue<Statistic>> buffer = getStatsBuffer();
        clearStatsBuffer();

        if(buffer.size() == 0)
            return null;

        // calcolo le statistiche locali di ogni nodo (media delle medie ricevute)
        for(String nodeId: buffer.keySet())
        {
            double value = buffer.get(nodeId).stream().mapToDouble(Statistic::getValue).average().orElse(0);

            Statistic s = Statistic.newBuilder().setNodeID(nodeId).setValue(value).setTimestamp(node.computeTimestamp()).build();
            System.out.println("COORDINATOR - local stat: "+s.getNodeID()+" "+value+" at " + s.getTimestamp());
            ls.add(s);
        }

        // calcolo la statistica globale
        double value = ls.stream().mapToDouble(Statistic::getValue).average().orElse(0);

        Statistic s = Statistic.newBuilder().setNodeID("Coord").setValue(value).setTimestamp(node.computeTimestamp()).build();
        lastGlobalStat = s;
        System.out.println("COORDINATOR - global stat: "+s.getNodeID()+" "+value+" at " + s.getTimestamp());

        ls.add(s);

        return ls;
    }

    public EdgeNode getNode()
    {
        return node;
    }
}
