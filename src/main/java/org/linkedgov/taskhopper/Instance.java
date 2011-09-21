package org.linkedgov.taskhopper;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.json.JSONWithPadding;
import java.io.IOException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;
import org.linkedgov.taskhopper.support.RDFToJSON;

public class Instance {
    private Document xml;

    public static Instance fromDocument(Document xml) {
        Instance instance = new Instance();
        instance.xml = xml;
        return instance;
    }

    public boolean hasContent() {
        if (this.xml == null) {
            return false;
        }
        Element root = xml.getRootElement();
        Elements empties = root.getChildElements("empty");
        return (empties.size() == 0);
    }

    public JSONObject toJSON() throws IOException, JSONException {
        JSONObject out = new JSONObject();
        JSONArray issues = new JSONArray();
        Elements elems = xml.getRootElement().getChildElements();
        for (int i = 0; i < elems.size(); i++) {
            Element elem = elems.get(i);
            if (elem.getLocalName().equals("dataset")) {
                JSONObject dataset = new JSONObject();
                dataset.putOpt("href", elem.getAttribute("href"));
                dataset.putOpt("title", elem.getAttribute("title"));
                dataset.putOpt("id", elem.getAttribute("id"));
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
                issue.putOpt("task-type", elem.getAttribute("task-type"));
                issue.putOpt("uri", elem.getAttribute("uri"));
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

    public JSONWithPadding toJSONP(String callback) throws IOException, JSONException {
        return new JSONWithPadding(this.toJSON(), callback);
    }

    public Document toXML() {
        return this.xml;
    }
}
