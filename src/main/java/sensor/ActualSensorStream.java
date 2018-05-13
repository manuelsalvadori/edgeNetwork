package sensor;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import edge_nodes.EdgeNode;
import edge_nodes.SensorGRPCGrpc;
import edge_nodes.SensorGRPCOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import simulation.Measurement;
import simulation.SensorStream;

public class ActualSensorStream implements SensorStream
{
    private volatile EdgeNode myEdgeNode;
    private Client sensorClient;
    private String serverUri;
    private int x;
    private int y;
    private Thread nodeUpdater;
    private String id;

    public ActualSensorStream(String id, Client sensorClient, String serverUri, EdgeNode myEdgeNode, int x, int y)
    {
        this.id = id;
        this.sensorClient = sensorClient;
        this.serverUri = serverUri;
        this.myEdgeNode = myEdgeNode;
        this.x = x;
        this.y = y;
        nodeUpdater = new Thread(new SensorUpdate(this));
        nodeUpdater.start();
    }

    @Override
    public void sendMeasurement(Measurement m)
    {
        if(myEdgeNode == null)
        {
            System.out.println(id+ " - no node available");
            return;
        }

        String measurement = null;
        try
        {
            measurement = new Gson().toJson(m);
        }
        catch (Exception e) { e.printStackTrace(); }

        assert measurement != null;

        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:"+ myEdgeNode.getSensorsPort()).usePlaintext(true).build();
        SensorGRPCGrpc.SensorGRPCBlockingStub stub = SensorGRPCGrpc.newBlockingStub(channel);
        SensorGRPCOuterClass.Measure request = SensorGRPCOuterClass.Measure.newBuilder().setM(measurement).build();

        try
        {
            stub.sendMeasure(request);
        }
        catch(StatusRuntimeException e)
        {
            nodeUpdater.interrupt();
        }

        channel.shutdown();
    }

    public Client getSensorClient()
    {
        return sensorClient;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public void setMyEdgeNode(EdgeNode edgeNode)
    {
        myEdgeNode = edgeNode;
    }

    public String getServerUri()
    {
        return serverUri;
    }

    public String getId()
    {
        return id;
    }
}
