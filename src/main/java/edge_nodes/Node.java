package edge_nodes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Node
{
    @XmlElement
    private String id;

    public Node() {}

    public Node(String node)
    {
        id = node;
    }

    public String getId()
    {
        return id;
    }
}
