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

    public synchronized boolean checkOKs()
    {
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
