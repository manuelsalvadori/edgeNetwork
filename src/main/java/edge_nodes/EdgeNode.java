package edge_nodes;

public class EdgeNode
{
    private String id;
    private String nodeURI;
    private int sensorsPort;
    private int nodesPort;
    private boolean isCoordinator;
    private int x;
    private int y;

    public EdgeNode(String id, String nodeURI, int sensorsPort, int nodesPort)
    {
        this.id = id;
        this.nodeURI = nodeURI;
        this.sensorsPort = sensorsPort;
        this.nodesPort = nodesPort;
        this.isCoordinator = false;
    }

    public String getId()
    {
        return id;
    }

    public String getNodeURI()
    {
        return nodeURI;
    }

    public int getSensorsPort()
    {
        return sensorsPort;
    }

    public int getNodesPort()
    {
        return nodesPort;
    }

    public int getY()
    {
        return y;
    }

    public int getX()
    {
        return x;
    }

    public boolean getIsCoordinator()
    {
        return isCoordinator;
    }

    public void setIsCoordinator(boolean isCoordinator)
    {
        this.isCoordinator = isCoordinator;
    }
}