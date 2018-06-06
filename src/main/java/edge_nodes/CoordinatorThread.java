package edge_nodes;

import java.util.*;
import edge_nodes.NodeGRPCOuterClass.Statistic;

public class CoordinatorThread implements Runnable // questo thread gestisce la logica del coordinatore
{
    private final EdgeNode node;
    private HashMap<String,PriorityQueue<Statistic>> statsBuffer;
    private volatile Statistic lastGlobalStat;
    private CoordinatorSender sender;

    CoordinatorThread(EdgeNode node)
    {
        this.node = node;
        lastGlobalStat = Statistic.newBuilder().setNodeID("Coord").setValue(0.0).setTimestamp(0).build();
        sender = new CoordinatorSender(this);
    }

    @Override
    public void run()
    {
        statsBuffer = new HashMap<>();

        // ogni 5 secondi invio le statistiche al server usando un thread apposito
        new Thread(sender).start();
    }

    synchronized Statistic addStatistic(Statistic s)
    {
//        // DEBUG - test concorrenza
//        System.out.println("+ **** DEBUG - addStatistic() sleeping ****");
//        try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }
//        System.out.println("- **** DEBUG - addStatistic() awaked ****");

        if(!statsBuffer.containsKey(s.getNodeID()))
            statsBuffer.put(s.getNodeID(),new PriorityQueue<>(20, Comparator.comparingLong(Statistic::getTimestamp)));

        statsBuffer.get(s.getNodeID()).offer(s);
        return lastGlobalStat.getTimestamp() == 0 ? node.getLastGlobalStat() : lastGlobalStat;
    }

    // ritorna una copia del buffer
    private synchronized HashMap<String, PriorityQueue<Statistic>> getStatsBuffer()
    {
        HashMap<String, PriorityQueue<Statistic>> copy = new HashMap<>();
        statsBuffer.keySet().forEach(k -> copy.put(k,new PriorityQueue<>(statsBuffer.get(k))));
        return copy;
    }

    private synchronized void clearStatsBuffer()
    {
        statsBuffer.clear();
    }

    // aggrego le statistiche per l'invio al server
    List<Statistic> computeStats()
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
            System.out.println("COORDINATOR - local stat:  "+s.getNodeID()+" "+value+" at " + s.getTimestamp());
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

    void stop()
    {
        this.sender.stop();
    }

    public EdgeNode getNode()
    {
        return node;
    }
}
