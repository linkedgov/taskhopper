package org.linkedgov.taskhopper.http;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import nu.xom.Document;
import nu.xom.ParsingException;
import org.linkedgov.taskhopper.Connection;
import org.linkedgov.taskhopper.TaskSelector;
import org.xml.sax.SAXException;

@Path("/task/{id: [0-9]+}")
public class ById {

    @GET
    @Produces("application/xml")
    public Response getXml(@PathParam("id") String reqId) throws IOException, SAXException, ParsingException {
        Connection conn = ApplicationSettings.getConnection();
        TaskSelector ts = new TaskSelector(conn);

        /* Handle requests for specific tasks by ID using get.xq */
        try {
            Document doc = ts.byId(reqId);
            return Support.respondIfNotEmpty(doc);
        } catch (IOException e) {
            return Response.serverError().build();
        } catch (SAXException e) {
            return Response.serverError().build();
        } catch (ParsingException e) {
            return Response.serverError().build();
        }
    }
}
