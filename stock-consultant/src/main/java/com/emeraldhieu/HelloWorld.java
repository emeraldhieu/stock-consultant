package com.emeraldhieu;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

@Path("")
@Component
public class HelloWorld {

    @GET
    @Path("/helloworld")
    public Response getHelloWorld() {
        String value = "Hello World";
        return Response.status(200).entity(value).build();
    }

    @GET
    @Path("")
    public Response getHomePage() {
        String value = "This is home page";
        return Response.status(200).entity(value).build();
    }
}