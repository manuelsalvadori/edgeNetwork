package cloud_services;

import edge_nodes.EdgeNode;

import java.util.HashSet;
import java.util.Set;

// this is a singleton
public class NodesGrid
{
    private static NodesGrid instance;
    private Set<EdgeNode> edgeNodeList;

    public NodesGrid()
    {
        this.edgeNodeList = new HashSet<>();
    }

    public synchronized static NodesGrid getInstance()
    {
        if(instance == null)
            instance = new NodesGrid();
        return instance;
    }

    public synchronized Set<EdgeNode> getEdgeNodeList()
    {
        return new HashSet<>(edgeNodeList);
    }

    public synchronized void addNode(EdgeNode node)
    {
        edgeNodeList.add(node);
    }

    public synchronized void removeNode(EdgeNode node)
    {
        edgeNodeList.remove(node);
    }

}
