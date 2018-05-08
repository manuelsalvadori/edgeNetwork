package edge_nodes;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.Scanner;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Node
{
    @XmlElement
    private String id;

    private String nodeURI;
    private int port;

    public Node() {}

    public Node(String id, String nodeURI, int port)
    {
        this.id = id;
        this.nodeURI = nodeURI;
        this.port = port;
    }

    public String getId()
    {
        return id;
    }

    public String getNodeURI()
    {
        return nodeURI;
    }

    public int getPort()
    {
        return port;
    }

    public static void main(String[] args )
    {
        Scanner io = new Scanner(System.in);
        int port = io.nextInt();

        try
        {

            Server server = ServerBuilder.forPort(port).addService(new SensorGRPCImpl()).build();

            server.start();

            System.out.println("Node started!");

            server.awaitTermination();

        }
        catch (IOException e) {e.printStackTrace();}
        catch (InterruptedException e) {e.printStackTrace();}


    }
}
