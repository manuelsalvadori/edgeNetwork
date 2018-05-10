package sensor;

import com.google.gson.Gson;
import com.google.protobuf.Empty;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import edge_nodes.Node;
import edge_nodes.SensorGRPCGrpc;
import edge_nodes.SensorGRPCOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.codehaus.jackson.map.ObjectMapper;
import simulation.Measurement;
import simulation.SensorStream;

import java.io.IOException;

public class ActualSensorStream implements SensorStream
{
    private volatile Node myNode;
    private Client sensorClient;
    private String serverUri;
    private int x;
    private int y;
    private Thread nodeUpdater;
    private String id;

    public ActualSensorStream(String id, Client sensorClient, String serverUri, Node myNode, int x, int y)
    {
        this.id = id;
        this.sensorClient = sensorClient;
        this.serverUri = serverUri;
        this.myNode = myNode;
        this.x = x;
        this.y = y;
        nodeUpdater = new Thread(new SensorUpdate(this));
        nodeUpdater.start();
    }

    @Override
    public void sendMeasurement(Measurement m)
    {
        if(myNode == null)
        {
            System.out.println("NULL");
            return;
        }

        String measurement = null;
        try
        {
            measurement = new Gson().toJson(m);//new ObjectMapper().writeValueAsString(m);
        }
        catch (Exception e) { e.printStackTrace(); }

        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:"+myNode.getPort()).usePlaintext(true).build();
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

    public void setMyNode(Node node)
    {
        myNode = node;
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
