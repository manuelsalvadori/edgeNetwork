package edge_nodes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class ParallelGrpcNewElection implements Runnable
{
    private final String targetID;
    private final String targetURI;
    private final EdgeNode node;

    ParallelGrpcNewElection(String targetID, String targetURI, EdgeNode node)
    {
        this.targetID = targetID;
        this.targetURI = targetURI;
        this.node = node;
    }

    @Override
    public void run()
    {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(targetURI).usePlaintext(true).build();
        NodeGRPCGrpc.NodeGRPCBlockingStub stub = NodeGRPCGrpc.newBlockingStub(channel);

        try
        {
            stub.newElection(NodeGRPCOuterClass.NodeURI.newBuilder()
                    .setNodeID(node.getId())
                    .setNodeURI(node.getNodeURI()+":"+node.getNodesPort())
                    .build());
        }
        catch (StatusRuntimeException e)
        {
            node.removeNodeFromLocalList(targetID);
        }
        channel.shutdown();
    }
}
