package edge_nodes;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class ParallelGrpcNewElection implements Runnable
{
    private String targetID;
    private String targetURI;
    private EdgeNode node;

    public ParallelGrpcNewElection(String targetID, String targetURI, EdgeNode node)
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
            stub.newElection(Empty.newBuilder().build());
        }
        catch (StatusRuntimeException e)
        {
            node.removeNodeFromLocalList(targetID);
        }
        channel.shutdown();
    }
}
