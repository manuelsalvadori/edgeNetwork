package edge_nodes;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class SensorGRPCImpl extends SensorGRPCGrpc.SensorGRPCImplBase
{
    @Override
    public void sendMeasure(SensorGRPCOuterClass.Measure request, StreamObserver<Empty> responseObserver)
    {

        System.out.println(request);

        Empty response = Empty.newBuilder().build();

        //passo la risposta nello stream
        responseObserver.onNext(response);

        responseObserver.onCompleted();

    }
}
