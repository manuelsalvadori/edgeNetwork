package edge_nodes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import edge_nodes.NodeGRPCOuterClass.NodeURI;

public class ParallelGrpcCoordFinder implements Runnable
{
    private final EdgeNode node;
    private final String targetID;
    private final String targetURI;
    private final int i;
    private final int count;
    private static int maxRPCs = 0;

    ParallelGrpcCoordFinder(EdgeNode node, String targetID, String targetURI, int i, int count)
    {
        this.node = node;
        this.targetID = targetID;
        this.targetURI = targetURI;
        this.i = i;
        this.count = count;
    }

    @Override
    public void run()
    {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(targetURI).usePlaintext(true).build();
        NodeGRPCGrpc.NodeGRPCBlockingStub stub = NodeGRPCGrpc.newBlockingStub(channel);

        NodeURI uri = NodeURI.newBuilder()
                .setNodeID(node.getId())
                .setNodeURI(node.getNodeURI()+":"+node.getNodesPort())
                .build();

        try
        {
            NodeGRPCOuterClass.Coordinator isCoordinator = stub.reportNewNode(uri);

            if (isCoordinator.getIsCoord())
                node.setCoordURI(targetURI);
            else
                node.addNodeToLocalList(targetID, targetURI);
        }
        catch (StatusRuntimeException e)
        {
            node.removeNodeFromLocalList(targetID);
        }

        // notifico il main thread solo quando tutte le grpc hanno risposto
        synchronized (node)
        {
            if(++maxRPCs == count)
            {
                node.notify();
            }
        }

        System.out.println("    - gRPC call " + i + " to " + targetID + " completed");
        channel.shutdown();
    }
}
