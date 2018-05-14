package edge_nodes;

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
        Coordinator response = Coordinator.newBuilder().setIsCoord(node.getIsCoordinator()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sendStatistic(Statistic request, StreamObserver<Statistic> responseObserver)
    {
        responseObserver.onNext(node.getCoordinatorThread().addStatistic(request));
        responseObserver.onCompleted();
    }
}
