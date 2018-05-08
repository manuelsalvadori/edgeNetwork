package edge_nodes;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Node
{
    @XmlElement
    private String id;

    private int nodeURI;

    public Node() {}

    public Node(String id, int nodeURI)
    {
        this.id = id;
        this.nodeURI = nodeURI;
    }

    public String getId()
    {
        return id;
    }

    public int getNodeURI()
    {
        return nodeURI;
    }

    public static void main( String[] args )
    {
        try
        {

            Server server = ServerBuilder.forPort(4000).addService(new SensorGRPCImpl()).build();

            server.start();

            System.out.println("Node started!");

            server.awaitTermination();

        }
        catch (IOException e) {e.printStackTrace();}
        catch (InterruptedException e) {e.printStackTrace();}


    }
}
