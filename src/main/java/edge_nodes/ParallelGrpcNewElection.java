package edge_nodes;

public class ParallelGrpcNewElection implements Runnable
{
    private String uri;
    private EdgeNode node;

    public ParallelGrpcNewElection(String uri, EdgeNode node)
    {
        this.uri = uri;
        this.node = node;
    }

    @Override
    public void run()
    {

    }
}
