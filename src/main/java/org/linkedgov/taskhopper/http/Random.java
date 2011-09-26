package org.linkedgov.taskhopper.http;

import com.sun.jersey.api.json.JSONWithPadding;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import nu.xom.ParsingException;
import org.codehaus.jettison.json.JSONException;
import org.linkedgov.taskhopper.Connection;
import org.linkedgov.taskhopper.Task;
import org.xml.sax.SAXException;

@Path("/task/random")
public class Random {

    @GET
    @Produces("application/xml")
    public Response getXml(
            @DefaultValue("") @QueryParam("type") String typeUrl) {

        Connection conn = ApplicationSettings.getConnection();
        Task.setConnection(conn);

        try {
            if (typeUrl.equals("") || typeUrl == null) {
                Task t = Task.random();
                if (t == null) {
                    // return 404 Not Found if there are no tasks left!
                    return Response.status(Response.Status.NOT_FOUND).build();
                } else {
                    org.w3c.dom.Document out = Support.xomToDom(t.toXML());
                    return Response.ok(out).build();
                }
            } else {
                Task t = Task.randomByType(typeUrl);
                if (t == null) {
                    // return 404 Not Found if there are no tasks left!
                    return Response.status(Response.Status.NOT_FOUND).build();
                } else {
                    org.w3c.dom.Document out = Support.xomToDom(t.toXML());
                    return Response.ok(out).build();
                }
            }
        } catch (IOException e) {
            return Response.serverError().build();
        } catch (SAXException e) {
            return Response.serverError().build();
        } catch (ParsingException e) {
            return Response.serverError().build();
        } catch (URISyntaxException e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Produces({"application/javascript", "application/json"})
    public Response getJson(@PathParam("id") String reqId,
            @DefaultValue("") @QueryParam("type") String typeUrl,
            @QueryParam("callback") @DefaultValue("callback") String callback) {

        Connection conn = ApplicationSettings.getConnection();
        Task.setConnection(conn);

        try {
            Task task;
            if (typeUrl.equals("") || typeUrl == null) {
                task = Task.random();
            } else {
                task = Task.randomByType(typeUrl);
            }
            if (callback == null || callback.equals("")) {
                return Response.ok(task.toJSON()).build();
            } else {
                JSONWithPadding json = new JSONWithPadding(task.toJSON(), callback);
                return Response.ok(json).build();
            }
        } catch (SAXException e) {
            return Response.serverError().build();
        } catch (IOException e) {
            return Response.serverError().build();
        } catch (ParsingException e) {
            return Response.serverError().build();
        } catch (URISyntaxException e) {
            return Response.serverError().build();
        } catch (JSONException e) {
            return Response.serverError().build();
        }
    }
}
