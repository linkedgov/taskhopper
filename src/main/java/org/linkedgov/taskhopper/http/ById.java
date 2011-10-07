package org.linkedgov.taskhopper.http;

import org.linkedgov.taskhopper.support.ResponseHelper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;
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
import org.linkedgov.taskhopper.support.Validation;
import org.xml.sax.SAXException;

/**
 * Retrieves tasks by ID and returns as XML or JSON, and accepts updates to
 * tasks.
 *
 * @author tom
 */
@Path("/task/{id: [0-9]+}")
public class ById {

    private Logger log = Logger.getLogger(ById.class.getName());

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
            return ResponseHelper.respondIfNotEmpty(doc);
        } catch (NullPointerException e) {
            return notFoundXml();
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

        /* JSONP hack: because JSONP cannot POST, we simulate POST
        with GET. */
        if (method != null && method.toUpperCase().equals("POST")) {
            return this.updateJson(reqId, action, value, callback);
        }

        Connection conn = ApplicationSettings.getConnection();
        Task.setConnection(conn);

        if (!Validation.checkSanityOfJSONPCallback(callback)) {
            return Response.noContent().entity("Invalid JSONP callback name.").build();
        }

        try {
            Task task = Task.byId(reqId);
            if (task != null) {
                return ResponseHelper.JSONOrJSONP(task.toJSON(), callback);
            } else {
                return notFoundJson();
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
                return ResponseHelper.respondIfNotEmpty(doc);
            } else if (action.equals("null")) {
                Task task = Task.byId(reqId);
                Document doc = task.nullify();
                return ResponseHelper.respondIfNotEmpty(doc);
            } else if (action.equals("okay")) {
                Task task = Task.byId(reqId);
                Document doc = task.okay();
                return ResponseHelper.respondIfNotEmpty(doc);
            } else if (action.equals("refer")) {
                Task task = Task.byId(reqId);
                Document doc = task.referToExpert();
                return ResponseHelper.respondIfNotEmpty(doc);
            } else {
                return Response.noContent().build();
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
            // Handle "Edit this task".
            if (action.equals("edit")) {
                Task task = Task.byId(reqId);
                if (task == null) {
                    log.info("Task not found: " + reqId);
                    return notFoundJson();
                } else {
                    Document doc = task.edit(value);
                    Instance inst = Instance.fromDocument(doc);
                    return ResponseHelper.JSONOrJSONP(inst.toJSON(), callback);
                }
                // Handle "nullify".
            } else if (action.equals("null")) {
                Task task = Task.byId(reqId);
                if (task == null) {
                    log.info("Task not found: " + reqId);
                    return notFoundJson();
                } else {
                    Document doc = task.nullify();
                    Instance inst = Instance.fromDocument(doc);
                    return ResponseHelper.JSONOrJSONP(inst.toJSON(), callback);
                }
                // Handle "okay".
            } else if (action.equals("okay")) {
                Task task = Task.byId(reqId);
                if (task == null) {
                    log.info("Task not found: " + reqId);
                    return notFoundJson();
                } else {
                    Document doc = task.okay();
                    Instance inst = Instance.fromDocument(doc);
                    return ResponseHelper.JSONOrJSONP(inst.toJSON(), callback);
                }
                // Handle "refer data to expert".
            } else if (action.equals("refer")) {
                Task task = Task.byId(reqId);
                if (task == null) {
                    log.info("Task not found: " + reqId);
                    return notFoundJson();
                } else {
                    Document doc = task.referToExpert();
                    Instance inst = Instance.fromDocument(doc);
                    return ResponseHelper.JSONOrJSONP(inst.toJSON(), callback);
                }
            } else {
                return Response.noContent().build();
            }
        } catch (NullPointerException e) {
            log.info("NullPointerException in ById.java: " + e);
            return Response.serverError().build();
        } catch (IOException e) {
            return Response.serverError().build();
        } catch (JSONException e) {
            return Response.serverError().build();
        } catch (SAXException e) {
            return Response.serverError().build();
        } catch (ParsingException e) {
            return Response.serverError().build();
        } catch (URISyntaxException e) {
            return Response.serverError().build();
        }
    }

    public Response notFoundXml() {
        return Response.status(404).entity("<rsp><error>No task with that ID.</error></rsp>").build();
    }

    public Response notFoundJson() {
        return Response.status(404).entity("{\"rsp\": {\"error\": \"No task with that ID.\"}}").build();
    }
}
