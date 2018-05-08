package sensor;

import org.codehaus.jackson.map.ObjectMapper;
import simulation.Measurement;

import java.io.IOException;

public class SensorsSimulator
{
    private static final String SERVER_URI =  "http://localhost:2018";
    private static int n = 1;

    public static void main(String[] args)
    {
        for (int i = 0; i < n; i++)
        {
            new SensorInitializer(SERVER_URI).runSensor();
        }
    }
}
