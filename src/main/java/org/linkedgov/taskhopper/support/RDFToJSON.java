package org.linkedgov.taskhopper.support;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;
import nu.xom.Element;

/**
 * Convenience class for turning RDF/XML (XOM) to RDF/JSON (Jettison).
 *
 * @author tom
 */
public class RDFToJSON {

    public static JSONObject rdfXmlToJson(Element xml) throws IOException, JSONException {
        Model model = ModelFactory.createDefaultModel();
        InputStream mainDocStream = new ByteArrayInputStream(
                xml.toXML().getBytes("UTF-8"));
        model.read(mainDocStream, "");
        JSONObject json = new JSONObject();
        ResIterator subjectIterator = model.listSubjects();
        while (subjectIterator.hasNext()) {
            Resource subjectResource = subjectIterator.nextResource();
            String key = "";
            if (subjectResource.isAnon()) {
                key = "_:" + subjectResource.asNode().getBlankNodeId().getLabelString();
            } else {
                key = subjectResource.getURI();
            }
            json.put(key, processProperties(subjectResource));
        }
        return json;
    }

    private static JSONObject processProperties(Resource subjectResource)
            throws IOException, JSONException {
        JSONObject out = new JSONObject();

        HashSet<Property> propertiesToProcess = new HashSet<Property>();
        StmtIterator allPropertiesIterator = subjectResource.listProperties();
        while (allPropertiesIterator.hasNext()) {
            Statement statement = allPropertiesIterator.nextStatement();
            if (!propertiesToProcess.contains(statement.getPredicate())) {
                propertiesToProcess.add(statement.getPredicate());
            }
        }
        for (Property property : propertiesToProcess) {
            String key = property.getURI();
            StmtIterator propertyIterator = subjectResource.listProperties(property);
            JSONArray propList = new JSONArray();
            while (propertyIterator.hasNext()) {
                Statement propertyStatement = propertyIterator.nextStatement();
                propList.put(processProperty(propertyStatement));
            }
            out.put(key, propList);
        }

        return out;
    }

    private static JSONObject processProperty(Statement property) throws IOException, JSONException {
        JSONObject out = new JSONObject();
        if (property.getObject().isURIResource()) {
            Resource r = (Resource) property.getObject();
            out.put("value", r.getURI());
            out.put("type", "uri");
        } else if (property.getObject().isLiteral()) {
            Literal l = (Literal) property.getObject();
            out.put("value", l.getLexicalForm());
            out.put("type", "literal");
            String languageValue = l.getLanguage();
            if (languageValue != null && !languageValue.trim().equals("")) {
                out.put("lang", languageValue);
            }
            String dataTypeValue = l.getDatatypeURI();
            if (dataTypeValue != null && !dataTypeValue.trim().equals("")) {
                out.put("datatype", dataTypeValue);
            }
        } else if (property.getObject().isAnon()) {
            out.put("value", property.getObject().asNode().getBlankNodeId().getLabelString());
            out.put("type", "bnode");
        }
        return out;
    }
}
