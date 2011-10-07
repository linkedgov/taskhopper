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
 * The logic of this class has been derived heavily from JSONJenaWriter, a Jena
 * Writer. This was created as we don't want a "writer", we want to turn a Jena
 * model into a Jettison JSONObject.
 *
 * @author tom
 */
public class RDFToJSON {

    /* In RDF JSON, each subject is a key, which has as a value a JSON Object
     * representing the property. Each property has an array, which has an
     * object for each object that matches the same subject and predicate.
     * That obejct is made up of type, value, lang and datatype.
     *
     * For instance:
     *  @prefix : <http://example.org/>
     *  :jane :age "30" .
     *  :john :knows :jane.
     *
     * Would be turned into the following JSON:
     *
     * {
     *       "http://example.org/jane": {
     *          "http://example.org/jane": [
     *              {
     *                  "type": "literal",
     *                  "value": "30"
     *              }
     *          ]
     *      },
     *      "http://example.org/john": {
     *          "http://example.org/knows": [
     *              {
     *                  "type": "uri",
     *                  "value": "http://example.org/jane"
     *              }
     *          ]
     *      }
     *  }
     *
     */
    
    /**
     * Convert XOM Element containing RDF/XML to Jettison JSONObject containing RDF/JSON.
     *
     * @param xml XOM Element containing RDF/XML
     * @return Jettison JSONObject
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject rdfXmlToJson(Element xml) throws IOException, JSONException {
        Model model = ModelFactory.createDefaultModel();
        InputStream mainDocStream = new ByteArrayInputStream(
                xml.toXML().getBytes("UTF-8"));
        model.read(mainDocStream, "");
        JSONObject json = new JSONObject();

        /* Iterate through every subject in the graph. */
        ResIterator subjectIterator = model.listSubjects();
        while (subjectIterator.hasNext()) {
            Resource subjectResource = subjectIterator.nextResource();
            String key = "";
            /* If the subject is a blank node (aka. anonymous node), we give it
             * the BNodeID label string that Jena has given it. */
            if (subjectResource.isAnon()) {
                key = "_:" + subjectResource.asNode().getBlankNodeId().getLabelString();
            } else {
                /* Otherwise we use the URI. */
                key = subjectResource.getURI();
            }
            /* Now we get the properties affiliated with the subject. */
            json.put(key, processProperties(subjectResource));
        }
        return json;
    }

    /**
     * Convert RDF resources into JSON object representing all of the properties
     * and objects of that resource.
     *
     * @param subjectResource
     * @return JSON object representing the predicates and objects of that resource.
     * @throws IOException
     * @throws JSONException
     */
    private static JSONObject processProperties(Resource subjectResource)
            throws IOException, JSONException {
        JSONObject out = new JSONObject();

        HashSet<Property> propertiesToProcess = new HashSet<Property>();
        /* Iterate through all properties, and if it is used by this subject,
         * add it to the set of properties to process. */
        StmtIterator allPropertiesIterator = subjectResource.listProperties();
        while (allPropertiesIterator.hasNext()) {
            Statement statement = allPropertiesIterator.nextStatement();
            if (!propertiesToProcess.contains(statement.getPredicate())) {
                propertiesToProcess.add(statement.getPredicate());
            }
        }
        /* Iterate through properties found, convert each into a JSON Object
         * and add result to an array, then attach that to the parent object. */
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

    /**
     * Convert individual statements into JSON representations of their object.
     *
     * @param property
     * @return RDF/JSON object component.
     * @throws IOException
     * @throws JSONException
     */
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
