package sensor;

public class SensorsSimulator
{
    private static final String SERVER_URI =  "http://localhost:2018";
    private static int n = 4;

    public static void main()
    {
        for (int i = 0; i < n; i++)
        {
            new Sensor(SERVER_URI).runSensor();
        }
    }
}
