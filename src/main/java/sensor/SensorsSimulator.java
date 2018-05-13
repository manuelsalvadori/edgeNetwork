package sensor;

public class SensorsSimulator
{
    private static final String SERVER_URI =  "http://localhost:2018";
    private static int n = 4;

    public static void main(String[] args)
    {
        for (int i = 1; i <= n; i++)
        {
            System.out.println(i);
            new SensorInitializer(SERVER_URI).runSensor("Sensor_"+i);
        }
    }
}
