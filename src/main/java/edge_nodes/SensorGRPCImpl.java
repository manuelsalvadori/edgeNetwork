package edge_nodes;

import com.google.gson.Gson;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.codehaus.jackson.map.ObjectMapper;
import simulation.Measurement;

import java.io.IOException;

public class SensorGRPCImpl extends SensorGRPCGrpc.SensorGRPCImplBase
{
    EdgeNode node;

    public SensorGRPCImpl(EdgeNode node)
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
