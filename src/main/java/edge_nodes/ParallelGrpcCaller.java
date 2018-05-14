package edge_nodes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class ParallelGrpcCaller implements Runnable
{
    private EdgeNode node;
    private String targetURI;
    private int i;

    public ParallelGrpcCaller(EdgeNode node, String targetURI, int i)
    {
        this.node = node;
        this.targetURI = targetURI;
        this.i = i;
    }

    @Override
    public void run()
    {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(targetURI).usePlaintext(true).build();

        NodeGRPCGrpc.NodeGRPCStub stub = NodeGRPCGrpc.newStub(channel);

        NodeGRPCOuterClass.NodeURI uri = NodeGRPCOuterClass.NodeURI.newBuilder().setNode(node.getNodeURI()+":"+node.getNodesPort()).build();
        try
        {
            stub.reportNewNode(uri, new StreamObserver<NodeGRPCOuterClass.Coordinator>()
            {
                @Override
                public void onNext(NodeGRPCOuterClass.Coordinator coordinator)
                {
                    if (coordinator.getIsCoord())
                        node.setCoordURI(targetURI);
                    else
                        node.addNodeToLocalList(targetURI);
                    node.decCounter();
                }

                @Override
                public void onError(Throwable throwable)
                {
                    node.decCounter();
                    System.out.println("gRPC Error! " + throwable.getMessage());
                }

                @Override
                public void onCompleted()
                {
                    channel.shutdownNow();
                }
            });

            System.out.println("rpc "+i+" completed");

        }
        catch (StatusRuntimeException e)
        {
            node.decCounter();
            node.removeNodeFromLocalList(targetURI);
            e.printStackTrace();
        }

        try
        {
            channel.awaitTermination(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {e.printStackTrace();}
    }
}
