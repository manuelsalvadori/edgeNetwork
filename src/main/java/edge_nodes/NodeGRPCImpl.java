package edge_nodes;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import edge_nodes.NodeGRPCOuterClass.Coordinator;
import edge_nodes.NodeGRPCOuterClass.NodeURI;
import edge_nodes.NodeGRPCOuterClass.Statistic;

public class NodeGRPCImpl extends NodeGRPCGrpc.NodeGRPCImplBase
{
    EdgeNode node;

    public NodeGRPCImpl(EdgeNode node)
    {
        this.node = node;
    }

    @Override
    public void reportNewNode(NodeURI request, StreamObserver<Coordinator> responseObserver)
    {
        node.addNodeToLocalList(request.getNodeID(), request.getNodeURI());
        responseObserver.onNext(Coordinator.newBuilder().setIsCoord(node.getIsCoordinator()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void sendStatistic(Statistic request, StreamObserver<Statistic> responseObserver)
    {
        responseObserver.onNext(node.getCoordinatorThread().addStatistic(request));
        responseObserver.onCompleted();
    }

    @Override
    public void newElection(NodeURI request, StreamObserver<Empty> responseObserver)
    {
        sendOk(request.getNodeID(),request.getNodeURI());
        node.newElection();
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void reportNewCoordinator(NodeURI request, StreamObserver<Empty> responseObserver)
    {
        node.setCoordinator(request.getNodeID(), request.getNodeURI());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void sendOK(Empty request, StreamObserver<Empty> responseObserver)
    {
        node.waitOKs.ok();
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    private void sendOk(String targetID, String targetURI)
    {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(targetURI).usePlaintext(true).build();
        NodeGRPCGrpc.NodeGRPCBlockingStub stub = NodeGRPCGrpc.newBlockingStub(channel);
        try
        {
            stub.sendOK(Empty.newBuilder().build());
        }
        catch (StatusRuntimeException e)
        {
            node.removeNodeFromLocalList(targetID);
        }
        channel.shutdown();
    }
}
