package org.linkedgov.taskhopper.http;

import com.sun.jersey.api.json.JSONWithPadding;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.converters.DOMConverter;
import org.codehaus.jettison.json.JSONObject;

public class Support {

    public static Response JSONOrJSONP(JSONObject json, String callback) {
        if (callback == null || callback.equals("")) {
            return Response.ok(json).build();
        } else {
            JSONWithPadding jsonp = new JSONWithPadding(json, callback);
            return Response.ok(jsonp).build();
        }
    }

    public static org.w3c.dom.Document xomToDom(Document doc) {
        try {
            return DOMConverter.convert(doc,
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation());
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isDocumentEmpty(Document doc) {
        Element root = doc.getRootElement();
        Elements empties = root.getChildElements("empty");
        return (empties.size() != 0);
    }

    public static Response respondIfNotEmpty(Document doc) {
        /* Check to see if the result is empty. If it is, we return
         * 404 Not Found status code. */
        if (isDocumentEmpty(doc)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(Support.xomToDom(doc)).build();
        }
    }

}
