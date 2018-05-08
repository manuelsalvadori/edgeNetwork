package sensor;

import com.google.protobuf.Empty;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import edge_nodes.Node;
import edge_nodes.SensorGRPCGrpc;
import edge_nodes.SensorGRPCOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import simulation.Measurement;
import simulation.SensorStream;

import java.io.IOException;

public class ActualSensorStream implements SensorStream
{
    private Node myNode;
    private Client sensorClient;
    private String serverUri;
    private int x;
    private int y;

    public ActualSensorStream(Client sensorClient, String serverUri, Node myNode, int x, int y)
    {
        this.sensorClient = sensorClient;
        this.serverUri = serverUri;
        this.myNode = myNode;
        this.x = x;
        this.y = y;
    }

    @Override
    public void sendMeasurement(Measurement m)
    {
        if(myNode == null)
            return;

        String measurement = null;
        try
        {
            measurement = new ObjectMapper().writeValueAsString(m);
        }
        catch (IOException e) { e.printStackTrace(); }

        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:4000").usePlaintext(true).build();

        SensorGRPCGrpc.SensorGRPCBlockingStub stub = SensorGRPCGrpc.newBlockingStub(channel);
        SensorGRPCOuterClass.Measure request = SensorGRPCOuterClass.Measure.newBuilder().setM(measurement).build();
        Empty response = stub.sendMeasure(request);

        //closing the channel
        channel.shutdown();

    }

    private Node retrieveNode(String uri)
    {
        WebResource webResource = sensorClient.resource(serverUri+"/SensorInit/"+x+"/"+y);
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

        if (response.getStatus() != 404 && response.getStatus() != 200)
            throw new RuntimeException("Failed sensor init: HTTP error code: " + response.getStatus());

        Node output = null;
        if(response.getStatus() != 404)
        {
            output = response.getEntity(Node.class);
            System.out.print("Received node; ID: " + output.getId());
        }
        else
            System.out.println("No node available");

        return output;
    }
}
