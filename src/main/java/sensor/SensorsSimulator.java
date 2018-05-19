package sensor;

import java.util.Scanner;

public class SensorsSimulator
{
    private static final String SERVER_URI =  "http://localhost:2018";

    public static void main(String[] args)
    {
        System.out.println("How many sensors?");
        int n = new Scanner(System.in).nextInt();

        for (int i = 1; i <= n; i++)
        {
            System.out.println(i);
            new SensorInitializer(SERVER_URI).runSensor("Sensor_"+i);
        }
    }
}