package analyst_application;

import java.util.Scanner;

public class AnalystApplication
{

    public static void main(String[] args)
    {
        Scanner scan = new Scanner(System.in);
        while(true)
        {
            System.out.println("- Analyst Console -");
            System.out.println("Press 1 to get the city state");
            System.out.println("Press 2 to get statistics from a chosen node");
            System.out.println("Press 3 to get global and local statistics");
            System.out.println("Press 4 to get standard deviation and mean from a chosen node");
            System.out.println("Press 5 to get standard deviation and mean from global statistics");
            System.out.println("Press any other key to exit");

            switch(scan.nextInt())
            {
                case 1:
                    getCityState();
                    break;
                case 2:
                    getNodeStats();
                    break;
                case 3:
                    getGlobalStats();
                    break;
                case 4:
                    getStandardDeviationNode();
                    break;
                case 5:
                    getStandardDeviationGlobal();
                    break;
                default:
                    System.exit(0);
            }
        }
    }

    private static void getCityState()
    {

    }

    private static void getNodeStats()
    {

    }

    private static void getGlobalStats()
    {

    }

    private static void getStandardDeviationNode()
    {

    }

    private static void getStandardDeviationGlobal()
    {

    }

}
