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
        //System.out.println("Insert server address:");
        //String serverURI = io.nextLine();
        String serverURI = "http://localhost:2018";
        System.out.println("Insert nodes id:");
        String nodeId = io.nextLine();

        EdgeNode node = new EdgeNode(nodeId, serverURI, sensorPort, nodePort);
        if(!node.nodeInit())
        {
            System.out.println("Node initialization failed - try again");
            return;
        }

        Server sensorserver = null, nodeserver = null;

        try
        {
            sensorserver = ServerBuilder.forPort(sensorPort)
                    .addService(new SensorGRPCImpl(node))
                    .build();
            sensorserver.start();

            nodeserver = ServerBuilder.forPort(nodePort)
                    .addService(new NodeGRPCImpl(node))
                    .build();
            nodeserver.start();

            System.out.println("Node "+ node.getId() +" started!");

        }
        catch (Exception e) {e.printStackTrace();}

        System.out.println("Enter exit to shutdown this node in any time");
        String exit = "";
        while(!exit.equals("exit"))
        {
            exit = io.nextLine();
        }

        sensorserver.shutdown();
        nodeserver.shutdown();

        System.out.println("Node terminated");
        while(!(sensorserver.isTerminated() && nodeserver.isTerminated()));
        node.removeFromCloud();
        System.exit(0);

    }
}
