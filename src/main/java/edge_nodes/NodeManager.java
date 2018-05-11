package edge_nodes;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.Scanner;

public class NodeManager
{
    public static void main(String[] args )
    {
        Scanner io = new Scanner(System.in);
        System.out.println("Node Initialization...");
        System.out.println("Insert sensors port:");
        int sensorPort = io.nextInt();
        System.out.println("Insert nodes port:");
        int nodePort = io.nextInt();
        io.nextLine();
        System.out.println("Insert server address:");
        String serverURI = io.nextLine();
        System.out.println("Insert nodes id:");
        String nodeId = io.nextLine();

        EdgeNode node = new EdgeNode(nodeId, serverURI, sensorPort, nodePort);
        node.nodeInit(sensorPort-4000,nodePort-4000);

        try
        {
            Server server = ServerBuilder.forPort(sensorPort)
                    .addService(new SensorGRPCImpl(node))
                    .build();
            server.start();
            System.out.println("Node "+ node.getId() +" started!");
            server.awaitTermination();
        }
        catch (Exception e) {e.printStackTrace();}

    }
}
