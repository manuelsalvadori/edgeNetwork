package analyst_application;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import java.io.IOException;
import java.util.List;
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

            switch(scan.nextLine())
            {
                case "1":
                    getCityState();
                    break;
                case "2":
                    getNodeStats();
                    break;
                case "3":
                    getGlobalStats();
                    break;
                case "4":
                    getStandardDeviationNode();
                    break;
                case "5":
                    getStandardDeviationGlobal();
                    break;
                default:
                    System.out.println("Exit successful");
                    System.exit(0);
            }
        }
    }

    private static void getCityState()
    {
        ClientResponse response;
        try
        {
            WebResource webResource = getClient().resource( "http://localhost:2018/getStatistics/cityState");
            response = webResource.accept("application/json").get(ClientResponse.class);
        }
        catch(ClientHandlerException ce)
        {
            System.out.println("Analyst Application - Server cloud connection refused");
            return;
        }

        switch (response.getStatus())
        {
            case 200:
                String json = response.getEntity(String.class);
                List<String> nodes = new Gson().fromJson(json, new TypeToken<List<String>>(){}.getType());
                nodes.forEach(System.out::println);
                break;

            default:
                System.out.println("Analyst Application - Failed retrieving statistics: HTTP error code: " + response.getStatus());
        }
        System.out.println("Press enter to continue...");
        new Scanner(System.in).nextLine();
    }

    private static void getNodeStats()
    {
        Scanner scan = new Scanner(System.in);
        System.out.println("Specify a node id: ");
        String nodeid = scan.nextLine();
        System.out.println("How many statistics? ");
        int n = scan.nextInt();
        scan.nextLine();

        ClientResponse response;
        try
        {
            WebResource webResource = getClient().resource( "http://localhost:2018/getStatistics/node/"+nodeid+"/"+n);
            response = webResource.accept("application/json").get(ClientResponse.class);
        }
        catch(ClientHandlerException ce)
        {
            System.out.println("Analyst Application - Server cloud connection refused");
            return;
        }

        switch (response.getStatus())
        {
            case 200:
                String json = response.getEntity(String.class);
                List<String> stats = new Gson().fromJson(json, new TypeToken<List<String>>(){}.getType());
                stats.forEach(System.out::println);
                break;

            case 404:
                System.out.println("Node not found: bad ID");
                break;

            default:
                System.out.println("Analyst Application - Failed retrieving statistics: HTTP error code: " + response.getStatus());
        }
        System.out.println("Press any key to continue...");
        try
        {
            System.in.read();
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    private static void getGlobalStats()
    {
        Scanner scan = new Scanner(System.in);
        System.out.println("How many global statistics? ");
        int n = scan.nextInt();
        scan.nextLine();

        ClientResponse response;
        try
        {
            WebResource webResource = getClient().resource( "http://localhost:2018/getStatistics/node/global/"+n);
            response = webResource.accept("application/json").get(ClientResponse.class);
        }
        catch(ClientHandlerException ce)
        {
            System.out.println("Analyst Application - Server cloud connection refused");
            return;
        }

        switch (response.getStatus())
        {
            case 200:
                List<String> stats = new Gson().fromJson(response.getEntity(String.class),
                        new TypeToken<List<String>>(){}.getType());
                stats.forEach(System.out::println);
                break;

            default:
                System.out.println("Analyst Application - Failed retrieving statistics: HTTP error code: " + response.getStatus());
        }
        System.out.println("Press enter to continue...");
        scan.nextLine();
    }

    private static void getStandardDeviationNode()
    {
        Scanner scan = new Scanner(System.in);
        System.out.println("Specify a node id: ");
        String nodeid = scan.nextLine();
        System.out.println("How many statistics? ");
        int n = scan.nextInt();
        scan.nextLine();

        ClientResponse response;
        try
        {
            WebResource webResource = getClient().resource( "http://localhost:2018/getStatistics/node/sd/"+nodeid+"/"+n);
            response = webResource.accept("application/json").get(ClientResponse.class);
        }
        catch(ClientHandlerException ce)
        {
            System.out.println("Analyst Application - Server cloud connection refused");
            return;
        }

        switch (response.getStatus())
        {
            case 200:
                String json = response.getEntity(String.class);
                String stats = new Gson().fromJson(json, String.class);
                System.out.println(stats);
                break;

            case 404:
                System.out.println("Node not found: bad ID");
                break;

            default:
                System.out.println("Analyst Application - Failed retrieving statistics: HTTP error code: " + response.getStatus());
        }
        System.out.println("Press enter to continue...");
        scan.nextLine();
    }

    private static void getStandardDeviationGlobal()
    {
        Scanner scan = new Scanner(System.in);
        System.out.println("How many global statistics? ");
        int n = scan.nextInt();
        scan.nextLine();

        ClientResponse response;
        try
        {
            WebResource webResource = getClient().resource( "http://localhost:2018/getStatistics/node/globalsd/"+n);
            response = webResource.accept("application/json").get(ClientResponse.class);
        }
        catch(ClientHandlerException ce)
        {
            System.out.println("Analyst Application - Server cloud connection refused");
            return;
        }

        if(response.getStatus() == 200)
            System.out.println(new Gson().fromJson(response.getEntity(String.class), String.class));
        else
            System.out.println("Analyst Application - Failed retrieving statistics: HTTP error code: " + response.getStatus());

        System.out.println("Press enter to continue...");
        scan.nextLine();
    }

    private static Client getClient()
    {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        return Client.create(config);
    }

}
