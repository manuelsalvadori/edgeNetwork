package edge_nodes;

import java.util.HashSet;
import java.util.Set;

// this is a singleton
public class NodesGrid
{
    private static NodesGrid instance;
    private Set<NodeIdentifier> nodeIdentifierList;

    public NodesGrid()
    {
        this.nodeIdentifierList = new HashSet<>();
    }

    public synchronized static NodesGrid getInstance()
    {
        if(instance == null)
            instance = new NodesGrid();
        return instance;
    }

    public synchronized Set<NodeIdentifier> getNodeIdentifierList()
    {
        return new HashSet<>(nodeIdentifierList);
    }

    public synchronized void addNode(NodeIdentifier node)
    {
        nodeIdentifierList.add(node);
    }

    public synchronized void removeNode(NodeIdentifier node)
    {
        nodeIdentifierList.remove(node);
    }
/*
        public NodeIdentifier getByName(String name){

            List<NodeIdentifier> usersCopy = getUserslist();

            for(NodeIdentifier u: usersCopy)
                if(u.getName().toLowerCase().equals(name.toLowerCase()))
                    return u;
            return null;
        }*/

}
