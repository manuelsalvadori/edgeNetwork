import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

public class ServerCloud
{

    private static final String HOST = "localhost";
    private static final int PORT = 2018;


    public static void main(String[] args) throws IOException
    {
        final String URI = "http://"+HOST+":"+PORT+"/";
        HttpServer server = HttpServerFactory.create(URI);
        server.start();

        System.out.println("Cloud server started on: " + URI);

        System.out.println("Press return to stop...");
        System.in.read();
        server.stop(0);
        System.out.println("Server stopped");
    }
}
