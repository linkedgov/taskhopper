package org.linkedgov.taskhopper;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.json.JSONWithPadding;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.converters.DOMConverter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;
import org.linkedgov.taskhopper.support.RDFToJSON;

/**
 * An instance (row, essentially) of data in a dataset.
 *
 * Class provides convenience methods for converting to JSON etc.
 *
 * @author tom
 */
public class Instance {
    private Document xml;

    /**
     * Creates an Instance object from XML (returned from the database).
     *
     * @param xml
     * @return an Instance object
     */
    public static Instance fromDocument(Document xml) {
        Instance instance = new Instance();
        instance.xml = xml;
        return instance;
    }

    /**
     * Returns true if there is no "empty" element in the doucment,
     * returns false otherwise.
     *
     * @return whether the document has any content
     */
    public boolean hasContent() {
        if (this.xml == null) {
            return false;
        }
        Element root = xml.getRootElement();
        Elements empties = root.getChildElements("empty");
        return (empties.size() == 0);
    }

    /**
     * Returns the data as a JSON object.
     *
     * Contains the same data as in the XML but in JSON.
     *
     * @return JSON object.
     * @throws IOException
     * @throws JSONException
     */
    public JSONObject toJSON() throws IOException, JSONException {
        JSONObject out = new JSONObject();
        JSONArray issues = new JSONArray();
        Elements elems = xml.getRootElement().getChildElements();

        for (int i = 0; i < elems.size(); i++) {
            Element elem = elems.get(i);

            if (elem.getLocalName().equals("dataset")) {
                JSONObject dataset = new JSONObject();
                dataset.putOpt("href", elem.getAttribute("href").getValue());
                dataset.putOpt("title", elem.getAttribute("title").getValue());
                dataset.putOpt("id", elem.getAttribute("id").getValue());
                if (dataset.length() != 0) {
                    out.put("dataset", dataset);
                }
            }

            if (elem.getLocalName().equals("main")) {
                JSONObject mainGraph = new JSONObject();
                JSONObject mainGraphSerialization = RDFToJSON.rdfXmlToJson(
                        elem.getFirstChildElement("RDF", RDF.getURI()));
                mainGraph.put("rdf", mainGraphSerialization);
                out.put("main", mainGraph);
            }

            if (elem.getLocalName().equals("issue")) {
                JSONObject issue = new JSONObject();
                issue.putOpt("task-type", elem.getAttribute("task-type").getValue());
                issue.putOpt("uri", elem.getAttribute("uri").getValue());
                JSONObject issueGraphSerialization = RDFToJSON.rdfXmlToJson(
                        elem.getFirstChildElement("RDF", RDF.getURI()));
                issue.put("rdf", issueGraphSerialization);
                issues.put(issue);
            }

        }

        if (issues.length() > 1) {
            out.put("issues", issues);
        }
        
        return out;
    }

    /**
     * Returns JSON with padding (JSONP).
     *
     * JSONP is JSON wrapped with prefixed function name.
     * Used to avoid cross-domain Ajax policies in web browsers.
     *
     * @param callback String used as padding function name
     * @return JSONP object
     * @throws IOException
     * @throws JSONException
     */
    public JSONWithPadding toJSONP(String callback) throws IOException, JSONException {
        return new JSONWithPadding(this.toJSON(), callback);
    }

    public Document toXML() {
        return this.xml;
    }

    /**
     * Converts from XOM to W3C DOM (used by Apache Jersey to return XML).
     *
     * @return W3C DOM Document object.
     */
    public org.w3c.dom.Document toW3CDOMDocument() {
        try {
            return DOMConverter.convert(this.xml,
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation());
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
