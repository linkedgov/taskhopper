package org.linkedgov.taskhopper.http;

import com.sun.jersey.api.json.JSONWithPadding;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import nu.xom.Document;
import nu.xom.ParsingException;
import org.codehaus.jettison.json.JSONException;
import org.linkedgov.taskhopper.Connection;
import org.linkedgov.taskhopper.Instance;
import org.linkedgov.taskhopper.Task;
import org.linkedgov.taskhopper.TaskSelector;
import org.xml.sax.SAXException;

@Path("/task/{id: [0-9]+}")
public class ById {

    @GET
    @Produces("application/xml")
    public Response getXml(@PathParam("id") String reqId)
            throws IOException, SAXException, ParsingException {
        Connection conn = ApplicationSettings.getConnection();
        Task.setConnection(conn);
        TaskSelector ts = new TaskSelector(conn);

        /* Handle requests for specific tasks by ID using get.xq */
        try {
            Document doc = Task.byId(reqId).toXML();
            return Support.respondIfNotEmpty(doc);
        } catch (IOException e) {
            return Response.serverError().build();
        } catch (SAXException e) {
            return Response.serverError().build();
        } catch (ParsingException e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Produces({"application/javascript", "application/json"})
    public Response getJson(@PathParam("id") String reqId,
            @QueryParam("method") String method,
            @QueryParam("action") String action,
            @QueryParam("value") String value,
            @QueryParam("callback") @DefaultValue("callback") String callback) {

        if (method != null && method.toUpperCase().equals("POST")) {
            return this.updateJson(reqId, action, value, callback);
        }

        Connection conn = ApplicationSettings.getConnection();
        Task.setConnection(conn);

        try {
            Task task = Task.byId(reqId);
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

    @POST
    @Produces("application/xml")
    public Response update(
            @PathParam("id") String reqId,
            @FormParam("action") String action,
            @FormParam("value") String value) {
        Connection conn = ApplicationSettings.getConnection();
        TaskSelector ts = new TaskSelector(conn);

        try {
            if (action.equals("edit")) {
                Task task = Task.byId(reqId);
                Document doc = task.edit(value);
                return Support.respondIfNotEmpty(doc);
            } else if (action.equals("null")) {
                Task task = Task.byId(reqId);
                Document doc = task.nullify();
                return Support.respondIfNotEmpty(doc);
            } else {
                return Response.noContent().build();
            }
        } catch (IOException e) {
            return Response.serverError().build();
        } catch (SAXException e) {
            return Response.serverError().build();
        } catch (ParsingException e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Produces({"application/json", "application/javascript"})
    public Response updateJson(
            @PathParam("id") String reqId,
            @FormParam("action") String action,
            @FormParam("value") String value,
            @QueryParam("callback") @DefaultValue("callback") String callback) {
        Connection conn = ApplicationSettings.getConnection();
        Task.setConnection(conn);

        try {
            if (action.equals("edit")) {
                Task task = Task.byId(reqId);
                Document doc = task.edit(value);
                Instance inst = Instance.fromDocument(doc);
                if (callback == null || callback.equals("")) {
                    return Response.ok(inst.toJSON()).build();
                } else {
                    return Response.ok(inst.toJSONP(callback)).build();
                }
            } else if (action.equals("null")) {
                Task task = Task.byId(reqId);
                Document doc = task.nullify();
                Instance inst = Instance.fromDocument(doc);
                if (callback == null || callback.equals("")) {
                    return Response.ok(inst.toJSON()).build();
                } else {
                    return Response.ok(inst.toJSONP(callback)).build();
                }
            } else {
                return Response.noContent().build();
            }
        } catch (IOException e) {
            return Response.serverError().build();
        } catch (JSONException e) {
            return Response.serverError().build();
        } catch (SAXException e) {
            return Response.serverError().build();
        } catch (ParsingException e) {
            return Response.serverError().build();
        }
    }
}
