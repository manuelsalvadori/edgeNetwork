package edge_nodes;

public class WaitForOKs implements Runnable
{
    private EdgeNode node;
    private int oks;

    public WaitForOKs(EdgeNode node)
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
        // aspetto la notify del primo 'ok' entro 5 secondi di timeout;
        // oltre 5 secondi assumo che tutti i nodi contattati siano usciti dalla rete
        try
        {
            wait(5000);
        }
        catch (InterruptedException e) { e.printStackTrace(); }

        if(oks > 0)
            return true;
        return false;
    }
}
