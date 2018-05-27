package edge_nodes;

import com.google.gson.Gson;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import simulation.Measurement;

public class SensorGRPCImpl extends SensorGRPCGrpc.SensorGRPCImplBase
{
    private final EdgeNode node;

    SensorGRPCImpl(EdgeNode node)
    {
        this.node = node;
    }

    @Override
    public void sendMeasure(SensorGRPCOuterClass.Measure request, StreamObserver<Empty> responseObserver)
    {
        Measurement m = null;
        try
        {
            m = new Gson().fromJson(request.getM(), Measurement.class);
        }
        catch (Exception e) { e.printStackTrace(); }

        //debug System.out.println("id: "+m.getId()+" value: "+m.getValue() + " time: "+m.getTimestamp());
        node.addMeasurement(m);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
