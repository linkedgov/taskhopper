package org.linkedgov.taskhopper.http;

import org.linkedgov.taskhopper.support.ResponseHelper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import org.linkedgov.taskhopper.Connection;
import org.linkedgov.taskhopper.TaskSelector;
import org.linkedgov.taskhopper.Task;
import org.xml.sax.SAXException;

/**
 * Imports issues into taskhopper from instance data.
 *
 * TODO: expand
 *
 * @author tom
 */
@Path("/task/import")
public class Import {

    @POST
    @Produces("application/xml")
    public Response importData(@QueryParam("url") String url) {
        try {
            Connection conn = ApplicationSettings.getConnection();
            Task.setConnection(conn);
            TaskSelector ts = new TaskSelector(conn);
            String decodedUrl = URLDecoder.decode(url, "UTF-8");
            ArrayList<Task> tasks = ts.importIssues(decodedUrl);

            /* Setup variables for output. */
            Nodes outputTaskNodes = new Nodes();
            int successCount = 0;
            int failureCount = 0;

            /* Iterate through the tasks and store them in the database. */
            for (Task task : tasks) {
                Document resp = task.create();
                boolean success = resp.getRootElement().getChildElements().size() > 0;
                if (success) {
                    successCount++;
                    Nodes successfulTasks = resp.getRootElement().query("task");
                    for (int i = 0; i < successfulTasks.size(); i++) {
                        outputTaskNodes.append(successfulTasks.get(i).copy());
                    }
                } else {
                    failureCount++;
                }
            }

            /* Format a response containing imported tasks and attributes to provide
             * success and failure counts. */
            Element root = new Element("rsp");
            root.addAttribute(new Attribute("action", "import"));
            root.addAttribute(new Attribute("success", Integer.toString(successCount)));
            root.addAttribute(new Attribute("failure", Integer.toString(failureCount)));
            for (int i = 0; i < outputTaskNodes.size(); i++) {
                root.appendChild(outputTaskNodes.get(i));
            }

            /* Write output. */
            Document outputDoc = new Document(root);
            return ResponseHelper.respondIfNotEmpty(outputDoc);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        } catch (ParsingException e) {
            return Response.serverError().build();
        } catch (IOException e) {
            return Response.serverError().build();
        } catch (URISyntaxException e) {
            return Response.serverError().build();
        } catch (SAXException e) {
            return Response.serverError().build();
        }
    }
}
