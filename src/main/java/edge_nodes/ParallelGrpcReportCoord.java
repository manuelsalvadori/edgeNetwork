package edge_nodes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class ParallelGrpcReportCoord implements Runnable
{
    private EdgeNode node;
    private String targetID;
    private String targetURI;

    public ParallelGrpcReportCoord(String targetID, String targetURI, EdgeNode node)
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
        NodeGRPCOuterClass.NodeURI nodeUri = NodeGRPCOuterClass.NodeURI.newBuilder().setNodeID(node.getId()).setNodeURI(node.getNodeURI()+":"+node.getNodesPort()).build();
        try
        {
            stub.reportNewCoordinator(nodeUri);
        }
        catch (StatusRuntimeException e)
        {
            node.removeNodeFromLocalList(targetID);
        }
        channel.shutdown();

    }
}
