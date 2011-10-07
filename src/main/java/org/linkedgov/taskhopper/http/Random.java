package org.linkedgov.taskhopper.http;

import org.linkedgov.taskhopper.support.ResponseHelper;
import com.sun.jersey.api.json.JSONWithPadding;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import nu.xom.Document;
import org.codehaus.jettison.json.JSONObject;
import org.linkedgov.taskhopper.Connection;
import org.linkedgov.taskhopper.Task;

@Path("/task/random")
public class Random {

    @GET
    @Produces("application/xml")
    public Response getXml(
            @DefaultValue("") @QueryParam("type") String typeUrl) {

        Connection conn = ApplicationSettings.getConnection();
        Task.setConnection(conn);

        if (typeUrl.equals("") || typeUrl == null) {
            Document doc = Task.randomWrappedXml();
            if (doc == null) {
                // return 404 Not Found if there are no tasks left!
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {
                org.w3c.dom.Document out = ResponseHelper.xomToDom(doc);
                return Response.ok(out).build();
            }
        } else {
            Document xml = Task.randomByTypeWrappedXml(typeUrl);
            if (xml == null) {
                // return 404 Not Found if there are no tasks left!
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {
                org.w3c.dom.Document out = ResponseHelper.xomToDom(xml);
                return Response.ok(out).build();
            }
        }
    }

    @GET
    @Produces({"application/javascript", "application/json"})
    public Response getJson(@PathParam("id") String reqId,
            @DefaultValue("") @QueryParam("type") String typeUrl,
            @QueryParam("callback") @DefaultValue("callback") String callback) {

        Connection conn = ApplicationSettings.getConnection();
        Task.setConnection(conn);

        JSONObject json;
        if (typeUrl.equals("") || typeUrl == null) {
            json = Task.randomWrappedJSON();
        } else {
            json = Task.randomByTypeWrappedJson(typeUrl);
        }
        if (callback == null || callback.equals("")) {
            return Response.ok(json).build();
        } else {
            JSONWithPadding jsonp = new JSONWithPadding(json, callback);
            return Response.ok(jsonp).build();
        }
    }
}
