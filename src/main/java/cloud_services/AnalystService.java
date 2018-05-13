package cloud_services;

import server_containers.CityStatistics;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("getStatistics")
public class AnalystService
{
    @GET
    @Path("cityState")
    @Produces("application/json")
    public Response getCityState()
    {
        CityStatistics.getInstance();

        return Response.ok().build();
    }
}
