package edge_nodes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class ParallelGrpcCoordFinder implements Runnable
{
    private EdgeNode node;
    private String targetID;
    private String targetURI;
    private int i;

    public ParallelGrpcCoordFinder(EdgeNode node, String targetID, String targetURI, int i)
    {
        this.node = node;
        this.targetID = targetID;
        this.targetURI = targetURI;
        this.i = i;
    }

    @Override
    public void run()
    {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(targetURI).usePlaintext(true).build();

        NodeGRPCGrpc.NodeGRPCBlockingStub stub = NodeGRPCGrpc.newBlockingStub(channel);

        NodeGRPCOuterClass.NodeURI uri = NodeGRPCOuterClass.NodeURI.newBuilder().setNodeID(node.getId()).setNodeURI(node.getNodeURI()+":"+node.getNodesPort()).build();
        try
        {
            NodeGRPCOuterClass.Coordinator coordinator = stub.reportNewNode(uri);

            if (coordinator.getIsCoord())
                node.setCoordURI(targetURI);
            else
                node.addNodeToLocalList(targetID, targetURI);
            node.decCounter();

        }
        catch (StatusRuntimeException e)
        {
            node.decCounter();
            node.removeNodeFromLocalList(targetID);
            e.printStackTrace();
        }

        System.out.println("    gRPC call"+i+" to "+targetID+" completed");
        channel.shutdown();
    }
}
