package edge_nodes;

public class NodeIdentifier
{
    private String id;
    private String nodeURI;
    private int sensorsPort;
    private int nodesPort;
    private int x;
    private int y;

    public NodeIdentifier(String id, String nodeURI, int sensorsPort, int nodesPort)
    {
        this.id = id;
        this.nodeURI = nodeURI;
        this.sensorsPort = sensorsPort;
        this.nodesPort = nodesPort;
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
}