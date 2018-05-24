package server_containers;

import edge_nodes.EdgeNode;

import java.util.HashSet;
import java.util.Set;

// this is a singleton
public class NodesGrid
{
    private static NodesGrid instance;
    private Set<EdgeNode> edgeNodeList;

    private NodesGrid()
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

    public synchronized boolean removeNode(String node)
    {
        for(EdgeNode n: edgeNodeList)
            if(n.getId().equals(node))
                return edgeNodeList.remove(n);
        return false;
    }

}
