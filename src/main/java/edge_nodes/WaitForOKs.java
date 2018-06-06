package edge_nodes;

public class WaitForOKs implements Runnable // thread che durante l'elezione riceve gli ok di risposta
{
    private final EdgeNode node;
    private int oks;

    WaitForOKs(EdgeNode node)
    {
        this.node = node;
        this.oks = 0;
    }

    @Override
    public void run()
    {
        if(!checkOKs())
            node.setMeAsCoordinator();
    }

    public synchronized void ok()
    {
        oks++;
        notify();
    }

    private synchronized boolean checkOKs()
    {
        // aspetto la notify del primo 'ok' entro 10 secondi di timeout;
        // oltre 10 secondi assumo che tutti i nodi contattati siano usciti dalla rete
        try
        {
            wait(10000);
        }
        catch (InterruptedException e) { e.printStackTrace(); }

        return oks > 0;
    }
}
