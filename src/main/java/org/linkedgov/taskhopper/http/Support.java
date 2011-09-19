package org.linkedgov.taskhopper.http;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.converters.DOMConverter;

public class Support {

    public static org.w3c.dom.Document xomToDom(Document doc) {
        try {
            return DOMConverter.convert(doc,
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation());
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Response respondIfNotEmpty(Document doc) {
        Element root = doc.getRootElement();
        Elements empties = root.getChildElements("empty");

        /* Check to see if the result is empty. If it is, we return
         * 404 Not Found status code. */
        if (empties.size() != 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(Support.xomToDom(doc)).build();
        }
    }
}
