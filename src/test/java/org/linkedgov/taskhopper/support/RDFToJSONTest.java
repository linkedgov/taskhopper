package org.linkedgov.taskhopper.support;

import junit.framework.TestCase;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class RDFToJSONTest extends TestCase {
    
    public RDFToJSONTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of rdfXmlToJson method, of class RDFToJSON.
     */
    public void testRdfXmlToJson() throws Exception {
        System.out.println("rdfXmlToJson");
        // define XML input
        String rdfEx = "<?xml version=\"1.0\" ?>\n" +
                "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                " xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n" +
                "<rdf:Description rdf:about=\"http://example.org/1\">\n" +
                "<dc:title>Example resource</dc:title>\n" +
                "</rdf:Description>\n" +
                "</rdf:RDF>\n";
        
        // parse into XOM document
        Builder builder = new Builder();
        Document doc = builder.build(rdfEx, "");
        Element xml = doc.getRootElement();
        
        // defining the expected output of the parser.
        JSONObject expValue = new JSONObject();
        expValue.put("value", "Example resource");
        expValue.put("type", "literal");
        JSONArray expValueArray = new JSONArray();
        expValueArray.put(expValue);
        JSONObject expProperty = new JSONObject();
        expProperty.put("http://purl.org/dc/elements/1.1/title", expValueArray);
        JSONObject expResult = new JSONObject();
        expResult.put("http://example.org/1", expProperty);
        
        // do transform, check result
        JSONObject result = RDFToJSON.rdfXmlToJson(xml);
        assertEquals(expResult.toString(), result.toString());
    }

}
