package org.linkedgov.taskhopper.http;

import org.linkedgov.taskhopper.support.ResponseHelper;
import com.sun.jersey.api.json.JSONWithPadding;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import nu.xom.Document;
import nu.xom.Element;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Path("/task/problem")
public class Problem {
    @GET
    @Produces("application/xml")
    public Response getXml() {
        Element root = new Element("rsp");
        Element ok = new Element("ok");
        root.appendChild(ok);
        Document out = new Document(root);
        return ResponseHelper.respondIfNotEmpty(out);
    }
    
    @GET
    @Produces({"application/javascript", "application/json"})
    public Response getJsonp(
            @QueryParam("callback") @DefaultValue("callback") String callback) {
        JSONObject out = new JSONObject();
        try {
            out.put("rsp", "ok");
        } catch(JSONException e) {}

        if (callback == null || callback.equals("")) {
            return Response.ok(out).build();
        } else {
            JSONWithPadding jsonp = new JSONWithPadding(out, callback);
            return Response.ok(jsonp).build();
        }
    }
}