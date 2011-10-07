package org.linkedgov.taskhopper.http;

import org.linkedgov.taskhopper.support.ResponseHelper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.linkedgov.taskhopper.Connection;
import org.linkedgov.taskhopper.TaskSelector;
import org.linkedgov.taskhopper.Task;
import org.linkedgov.taskhopper.Instance;
import org.xml.sax.SAXException;

/**
 * Adds an individual new task.
 *
 * @author tom
 */
@Path("/task/new")
public class Input {

    @POST
    @Produces("application/xml")
    public Response addTask(
            @QueryParam("issue-uri") String issueUri,
            @QueryParam("graph-uri") String graphUri,
            @QueryParam("task-type") String taskType) {
        boolean willStore = true;
        ArrayList<String> errors = new ArrayList<String>();

        if (issueUri == null || issueUri.isEmpty()) {
            willStore = false;
            errors.add("The issue URI has not been set.");
        }

        if (graphUri == null || graphUri.isEmpty()) {
            willStore = false;
            errors.add("The graph URI has not been set.");
        }

        if (taskType == null || taskType.isEmpty()) {
            willStore = false;
            errors.add("The task type has not been set.");
        }
        
        /* If there are any errors, respond with an error response.
         * 400 Bad Request */
        if (willStore == false) {
            Element root = new Element("error");
            for (String error : errors) {
                Element li = new Element("li");
                li.appendChild(new nu.xom.Text(error));
                root.appendChild(li);
            }
            Document doc = new Document(root);
            Instance inst = Instance.fromDocument(doc);
            org.w3c.dom.Document out = ResponseHelper.xomToDom(doc);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(inst.toW3CDOMDocument()).build();
        }

        /* If there aren't any errors, store in the database and send response. */
        try {
            Connection conn = ApplicationSettings.getConnection();
            Task.setConnection(conn);
            TaskSelector ts = new TaskSelector(conn);
            Task task = new Task(taskType, issueUri, graphUri);
            Document dbResponse = task.create();
            return ResponseHelper.respondIfNotEmpty(dbResponse);
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
}
