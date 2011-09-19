package org.linkedgov.taskhopper.http;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import nu.xom.Document;
import nu.xom.ParsingException;
import org.linkedgov.taskhopper.Connection;
import org.linkedgov.taskhopper.TaskSelector;
import org.xml.sax.SAXException;

@Path("/task/random")
public class Random {

    @GET
    @Produces("application/xml")
    public Response getXmlByType(
            @DefaultValue("") @QueryParam("type") String typeUrl
        ) {

        Connection conn = ApplicationSettings.getConnection();
        TaskSelector ts = new TaskSelector(conn);

        try {
            if (typeUrl.equals("") || typeUrl == null) {
                Document doc = ts.random();
                return Support.respondIfNotEmpty(doc);
            } else {
                Document doc = ts.randomByType(typeUrl);
                return Support.respondIfNotEmpty(doc);
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
}
