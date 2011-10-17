package org.linkedgov.taskhopper;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.linkedgov.taskhopper.thirdparty.URIBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONException;
import org.xml.sax.SAXException;

/**
 * A task contains all the data necessary to give to a user in order to
 * fix an issue in the data: this includes an ID, a task type (URI),
 * an issue URI and a graph URI (which points to the individual data
 * instance - i.e. the 'row' in spreadsheet/databaset terminology).
 *
 * Inside tasks.xml, the tasks are of the form:
 *
 *  <pre>
 *  {@code
 *  <task id="1">
 *      <task-type href="http://linkedgov.org/schema/task-types/float-error"/>
 *      <graph-uri href="http://localhost:8080/exist/rest//db/linkedgov/dwp-electricity-use/1"/>
 *      <issue-uri href="http://linkedgov.org/data/dwp-electricity-use/1/issue/1"/>
 *   </task>
 *  }
 *  </pre>
 *
 * The task-type, graph-uri and issue-uri elements match up to the taskType,
 * issueUri and graphUri properties of the Task object. The id attribute
 * matches up with the id property.
 *
 * Storing a new task in the database is done through constructing
 * a <code>Task</code> object and then using the <code>create</code> method.
 *
 * Modifying the tasks is done using the <code>nullify</code>, <code>okay</code>,
 * <code>referToExpert</code> and <code>edit</code> methods.
 *
 * Each task is in a <code>Dataset</code> which can be accessed using <code>getDataset</code>.
 *
 * Once a task is done, to remove it from tasks.xml, call <code>removeFromHopper</code>.
 *
 * The output as served to the user of the task hopper is constructed by retrieving
 * the broken value and example data from the database: this is done with the
 * <code>getIssueValuesMap</code> method, as well as the <code>getExampleData</code> method.
 * 
 * @author tom
 */
public class Task {
    private static Logger logger = Logger.getLogger(Task.class.getName());
    private static void log(Exception ex) {
        Task.logger.log(Level.SEVERE, null, ex);
    }

    // <editor-fold defaultstate="collapsed" desc="String id;">

    private String id;

    /**
     * @return the id of the task
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="String taskType;">
    private String taskType;

    /**
     * @return URI of the task type.
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * @param taskType URI of the task type.
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="String issueUri;">
    private String issueUri;

    /**
     * @return the issue URI
     */
    public String getIssueUri() {
        return issueUri;
    }

    /**
     * @param issueUri URI of the issue.
     */
    public void setIssueUri(String issueUri) {
        this.issueUri = issueUri;
    }// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="String graphUri;">
    private String graphUri;

    /**
     * @return URI of the graph (and the document).
     */
    public String getGraphUri() {
        return graphUri;
    }

    /**
     * @param graphUri URI of the graph (and the document).
     */
    public void setGraphUri(String graphUri) {
        this.graphUri = graphUri;
    }// </editor-fold>
    private Document xml;

    /*
     * inDatabase tells you whether the object is actually in the database or not.
     */
    private boolean inDatabase = false;

    /*
     * connection holds a Connection object which stores and makes calls to the database.
     *
     */
    // <editor-fold defaultstate="collapsed" desc="static Connection connection;">
    private static Connection connection;

    public static void setConnection(Connection conn) {
        Task.connection = conn;
    }

    public static Connection getConnection() throws ConnectionNotFoundException {
        if (Task.connection == null) {
            throw new ConnectionNotFoundException(
                    "You need to set an connection by calling Task.setConnection()");
        } else {
            return Task.connection;
        }
    }
    // </editor-fold>

    /**
     * Creates a task object but doesn't save to the database until you call
     * <code>save()</code>, <code>create()</code> or <code>update()</code>.
     *
     * @param taskType URI of the task type.
     * @param issueUri URI of the issue.
     * @param graphUri URI of the graph.
     */
    public Task(String taskType, String issueUri, String graphUri) {
        if (taskType != null) {
            this.setTaskType(taskType);
        }

        if (issueUri != null) {
            this.setIssueUri(issueUri);
        }

        if (graphUri != null) {
            this.setGraphUri(graphUri);
        }
    }

    /**
     * Gets a task from the database by ID.
     *
     * @param taskId The ID of the task.
     * @return Task
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public static Task byId(String taskId)
            throws IOException, SAXException, ParsingException, ConnectionNotFoundException {

        Document xml = Task.getConnection().loadDocument("get.xq?id=" + taskId, null);
        Task t = Task.xmlToTask(xml);

        if (t != null) {
            t.rebuildXml(5);
            return t;
        } else {
            return null;
        }
    }

    /**
     * Gets a task from the database randomly.
     *
     * @return Task
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public static Task random() throws IOException, SAXException, ParsingException {
        Document xml = Task.connection.loadDocument("get_random.xq", null);
        if (xml.getRootElement().getChildElements().size() == 0) {
            return null;
        } else {
            Task t = Task.xmlToTask(xml);
            t.xml = xml;
            t.rebuildXml(5);
            return t;
        }
    }

    /**
     * Returns a random task wrapped in <code>rsp</code> element.
     *
     * @return Document an XML document containing an individual task wrapped.
     */
    public static Document randomWrappedXml() {
        Document xml = new Document(new Element("rsp"));

        try {
            Task t = Task.random();

            if (t != null) {
                Element root = (Element) t.toXML().getRootElement().copy();
                xml.getRootElement().appendChild(root);
            }
        } catch (IOException ex) {
            Task.log(ex);
        } catch (SAXException ex) {
            Task.log(ex);
        } catch (ParsingException ex) {
            Task.log(ex);
        }

        return xml;
    }

    /**
     * Returns a random task wrapped in a JSON object.
     *
     * @return JSONObject a JSON document containing an individual task wrapped.
     */
    public static JSONObject randomWrappedJSON() {
        JSONObject out = new JSONObject();
        JSONArray json = new JSONArray();
        Task t;

        try {
            t = Task.random();

            if (t != null) {
                json.put(t.toJSON());
            }

            out.put("rsp", json);
        } catch (IOException ex) {
            Task.log(ex);
        } catch (SAXException ex) {
            Task.log(ex);
        } catch (ParsingException ex) {
            Task.log(ex);
        } catch (URISyntaxException ex) {
            Task.log(ex);
        } catch (JSONException ex) {
            Task.log(ex);
        }

        return out;
    }

    /**
     * Get random task from database selected by type.
     *
     * @return Task a random task selected by type or null if no task available
     */
    public static Task randomByType(String type)
            throws IOException, SAXException, ParsingException, URISyntaxException {
        URIBuilder uri = new URIBuilder("random_by_type.xq");
        uri.addQueryParam("type", type);
        Document xml = Task.getConnection().loadDocument(uri);

        if (xml.getRootElement().getChildElements().size() == 0) {
            return null;
        } else {
            Task t = Task.xmlToTask(xml);
            t.xml = xml;
            t.rebuildXml(5);
            return t;
        }
    }

    /**
     * Get random task from database selected by type and wrap in XML response.
     *
     * @return Document an XML document containing a task if one is available
     */
    public static Document randomByTypeWrappedXml(String type)
    {
        Document xml = new Document(new Element("rsp"));

        try {
            Task t = Task.randomByType(type);
            Element root = (Element) t.toXML().getRootElement().copy();
            xml.getRootElement().appendChild(root);
        } catch (IOException ex) {
            Task.log(ex);
        } catch (SAXException ex) {
            Task.log(ex);
        } catch (ParsingException ex) {
            Task.log(ex);
        } catch (URISyntaxException ex) {
            Task.log(ex);
        }

        return xml;
    }

    /**
     * Get random task from database selected by type and wrap in JSON response.
     *
     * @return JSONObject a JSON object containing a task if one is available
     */
    public static JSONObject randomByTypeWrappedJson(String type)
    {
        JSONObject out = new JSONObject();
        JSONArray json = new JSONArray();

        try {
            Task t = Task.randomByType(type);
            if (t != null) {
                json.put(t.toJSON());
            }
            out.put("rsp", json);
        } catch (IOException ex) {
            Task.log(ex);
        } catch (SAXException ex) {
            Task.log(ex);
        } catch (ParsingException ex) {
            Task.log(ex);
        } catch (URISyntaxException ex) {
            Task.log(ex);
        } catch (JSONException ex) {
            Task.log(ex);
        }

        return out;
    }

    /**
     * Turn XML task document into Task.
     *
     * @param xml
     * @return Task instance.
     */
    public static Task xmlToTask(Document xml) {
        try {
            Element root = xml.getRootElement();
            Element task = root.getFirstChildElement("task");
            String aTaskType = task.getFirstChildElement("task-type").getAttribute("href").getValue();
            String aGraphUri = task.getFirstChildElement("graph-uri").getAttribute("href").getValue();
            String aIssueUri = task.getFirstChildElement("issue-uri").getAttribute("href").getValue();
            String aId = task.getAttribute("id").getValue();
            Task t = new Task(aTaskType, aIssueUri, aGraphUri);
            t.setId(aId);
            t.xml = xml;

            return t;
        } catch(NullPointerException e) {
            return null;
        }
    }

    /**
     * Creates the task in the database.
     *
     * @return the resulting document in the database
     * @throws URISyntaxException
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public Document create()
            throws URISyntaxException, IOException, SAXException, ParsingException {
        Map<String, String> params = new HashMap<String, String>();

        if (this.getIssueUri() != null && this.getIssueUri().length() > 0) {
            params.put("issue-uri", this.getIssueUri());
        }

        if (this.getTaskType() != null && this.getTaskType().length() > 0) {
            params.put("task-type", this.getTaskType());
        }

        if (this.getGraphUri() != null && this.getGraphUri().length() > 0) {
            params.put("graph-uri", this.getGraphUri());
        }

<<<<<<< HEAD
        if (this.id != null && this.id.length() > 0) {
            params.put("id", this.id);
        }

        URIBuilder uri = new URIBuilder("new.xq");
=======
        URIBuilder uri = new URIBuilder(ApplicationSettings.xquery.get("new"));
>>>>>>> c5d9f48... no longer submitting redundant task IDs
        uri.addQueryParams(params);
        Document xmlResp = Task.getConnection().loadDocument(uri);
        // TODO: parse response back out and modify state of the object... in a separate method
        return xmlResp;
    }
    
    /**
     * Nullifies the data in the database; removes task from taskhopper.
     *
     * @return Document containing the updated graph.
     * @throws ParsingException
     * @throws IOException
     */
    public Document nullify()
            throws ParsingException, IOException, UnsupportedEncodingException, ValidityException, ClassNotFoundException {

        Document input = Task.getConnection().loadUrl(this.getGraphUri());
        Document output = TaskUpdater.nullifyTask(input, this.getIssueUri());
        boolean resp = Task.getConnection().putDocument(output, this.getGraphUri());

        if (resp == true) {
            boolean removed = this.removeFromHopper();
        }

        return output;
    }

    /**
     * Marks data as okay in the database; removes task from taskhopper.
     *
     * When data is marked as okay, the value is moved in to the main
     * graph without change. It is equivalent to using <code>edit</code>
     * but submitting back the same value as given.
     *
     * @return
     * @throws ParsingException
     * @throws IOException
     */
    public Document okay()
            throws ParsingException, IOException {

        Document input = Task.getConnection().loadUrl(this.getGraphUri());
        Document output = TaskUpdater.markAsOkay(input, this.getIssueUri());
        boolean resp = Task.getConnection().putDocument(output, this.getGraphUri());

        if (resp == true) {
            boolean removed = this.removeFromHopper();
        }

        return output;
    }

    /**
     * Marks a task as needing referral to an expert.
     *
     * @return
     * @throws ParsingException
     * @throws IOException
     * @throws URISyntaxException
     * @throws SAXException
     */
    public Document referToExpert()
            throws ParsingException, IOException, URISyntaxException, SAXException {

        Document input = Task.getConnection().loadUrl(this.getGraphUri());
        Document output = TaskUpdater.referToExpert(input, this.getIssueUri());
        boolean resp = Task.getConnection().putDocument(output, this.getGraphUri());

        /* Unlike the other "update" tasks, we don't have to delete the task.
         * When we nullify a task or edit the value or mark it as okay, we
         * delete the task from the hopper. Here we are modifying the task in
         * place.  We do this by modifying the document, and then reimporting
         * it into the taskhopper.
         * 
         * No need to call removeFromHopper as the XQuery to reimport the
         * task should do the job. And if it fails, it's probably safer to
         * have two tasks in the task list.
         */

        TaskSelector ts = new TaskSelector(Task.getConnection());
        ArrayList<Task> tasks = ts.importIssues(this.getGraphUri());

        for (Task task : tasks) {
           Logger.getLogger(Task.class.getName()).info(task.toString());
           task.create();
        }

        return output;
    }

    /**
     * Modifies the incorrect value in the database to the supplied value;
     * removes the task from the taskhopper (if successful).
     * 
     * @param value
     * @return Document containing the updated graph.
     * @throws ParsingException
     * @throws IOException
     */
    public Document edit(String value)
            throws ParsingException, IOException {

        Document input = Task.getConnection().loadUrl(this.getGraphUri());
        Document output = TaskUpdater.editValue(input, this.getIssueUri(), value, null);
        boolean resp = Task.getConnection().putDocument(output, this.getGraphUri());

        if (resp == true) {
            boolean removed = this.removeFromHopper();
        }

        return output;
    }

    /**
     * Returns a Dataset object for a particular task.
     *
     * @return dataset
     * @throws ParsingException
     * @throws IOException
     */
    public Dataset getDataset() throws ParsingException, IOException {
        Document graphDoc = Task.getConnection().loadUrl(this.getGraphUri());
        Nodes datasetElems = graphDoc.getRootElement().query("dataset");
        Attribute href = null;
        Attribute title = null;
        Attribute datasetId = null;

        for (int i = 0; i < datasetElems.size(); i++) {
            Element datasetElem = (Element) datasetElems.get(i);
            title = datasetElem.getAttribute("title");
            href = datasetElem.getAttribute("href");
            datasetId = datasetElem.getAttribute("id");
        }

        Dataset dataset = new Dataset(title.getValue(),
            href.getValue(), datasetId.getValue());
        dataset.setConnection(Task.getConnection());

        return dataset;
    }

    /**
     * Gets valid example data from the dataset that the task is a member of, up to a certain maximum.
     *
     * @param maximum the maximum number of example entries you want
     * @return a list of the example data
     */
    public ArrayList<String> getExampleData(int maximum)
            throws ParsingException, IOException, URISyntaxException, SAXException {

        Dataset dataset = this.getDataset();
        String issuePredicate = this.getIssuePredicate();
        ArrayList<String> out = dataset.getExampleData(issuePredicate, maximum);

        return out;
    }

    /**
     * Retrieves the issue predicate URI as string from the issue graph.
     * 
     * @return predicate URI string for the issue this task represents.
     * @throws ParsingException
     * @throws IOException
     */
    public String getIssuePredicate() throws ParsingException, IOException {
        Document xmlResp = Task.getConnection().loadUrl(this.getGraphUri());
        Model issueGraph = TaskUpdater.getIssueGraphFromDocument(xmlResp, this.getIssueUri());
        StmtIterator stmts = issueGraph.listStatements();
        Property predicate = null;

        while (stmts.hasNext()) {
            Statement stmt = (Statement) stmts.next();
            predicate = stmt.getPredicate();
        }

        return predicate.getURI();
    }

    /**
     * Gets broken value from the document.
     *
     * @return String value of the broken value.
     */
    public String getBrokenValue() throws ParsingException, IOException {
        Map<String, String> issueValues = this.getIssueValuesMap();
        return issueValues.get("value");
    }

    /**
     * Get map containing the value and datatype of the object represented in the issue graph.
     *
     * The returned value doesn't necessarily match the specified type. The type returned is
     * the type the data should be rather than the type the data actually is.
     *
     * If there is no data in the issue graph (or there is data but the object of the statement
     * is not a literal), it will return an empty map.
     *
     * @return map with two values: type (String: an XSD type URI) and value (String)
     */
    public Map<String, String> getIssueValuesMap() throws ParsingException, IOException {
        Map<String, String> out = new HashMap<String, String>();
        Document xmlResp = Task.getConnection().loadUrl(this.getGraphUri());

        /* Retrieve the task graph from the document */
        Model issueGraph = TaskUpdater.getIssueGraphFromDocument(xmlResp, this.getIssueUri());
        String resp = null;
        String datatype = null;

        /* Iterate through (what should be a single statement) graph. */
        // assert (issueGraph.size() == 1);
        StmtIterator stmts = issueGraph.listStatements();
        while (stmts.hasNext()) {
            Statement stmt = (Statement) stmts.next();
            RDFNode object = stmt.getObject();
            if (object.isLiteral()) {
                Literal objectLiteral = (Literal) object;
                resp = objectLiteral.getLexicalForm();
                datatype = objectLiteral.getDatatypeURI();
                out.put("type", datatype);
                out.put("value", resp);
            }
        }
        return out;
    }

    /**
     * Rebuilds XML representation of the issue from the object.
     *
     * The response can then be retrieved using <code>toXML</code>
     *
     * @param maximum Maximum number of example values to return.
     */
    public void rebuildXml(int maximum) {
        Element root = new Element("task");
        root.addAttribute(new Attribute("id", this.getId()));

        // Add task type.
        if (this.getTaskType() != null) {
            Element taskTypeElem = new Element("task-type");
            taskTypeElem.addAttribute(new Attribute("task-type", this.getTaskType()));
            root.appendChild(taskTypeElem);
        }

        // Add graph URI.
        if (this.getGraphUri() != null) {
            Element graphUriElem = new Element("graph-uri");
            graphUriElem.addAttribute(new Attribute("graph-uri", this.getGraphUri()));
            root.appendChild(graphUriElem);
        }

        // Add issue URI.
        if (this.getIssueUri() != null) {
            Element issueUriElem = new Element("issue-uri");
            issueUriElem.addAttribute(new Attribute("issue-uri", this.getIssueUri()));
            root.appendChild(issueUriElem);
        }

        try {
            // Add issue predicate.
            String issuePred = this.getIssuePredicate();
            if (issuePred != null) {
                Element propertyElem = new Element("property");
                propertyElem.addAttribute(new Attribute("property", issuePred));
                root.appendChild(propertyElem);
            }

            // Add broken value.
            Map<String, String> brokenValueMap = this.getIssueValuesMap();
            if (brokenValueMap != null) {
                Element brokenValueElem = new Element("broken-value");

                if (brokenValueMap.containsKey("datatype")) {
                    brokenValueElem.addAttribute(
                            new Attribute("datatype", brokenValueMap.get("datatype")));
                }

                if (brokenValueMap.containsKey("value")) {
                    brokenValueElem.appendChild(brokenValueMap.get("value"));
                    root.appendChild(brokenValueElem);
                }
            }

            // Add example data array.
            ArrayList<String> exampleData = this.getExampleData(maximum);
            if (exampleData != null && !(exampleData.isEmpty())) {
                Element exampleDataElem = new Element("example-data");

                for (String example : exampleData) {
                    Element exElem = new Element("li");
                    exElem.appendChild(example);
                    exampleDataElem.appendChild(exElem);
                }

                root.appendChild(exampleDataElem);
            }

        } catch (ParsingException ex) {
            Task.log(ex);
        } catch (IOException ex) {
            Task.log(ex);
        } catch (URISyntaxException ex) {
            Task.log(ex);
        } catch (SAXException ex) {
            Task.log(ex);
        }

        Document doc = new Document(root);
        this.xml = doc;
    }

    /**
     * Rebuild XML with a default number of example data (5).
     */
    public void rebuildXml() {
        this.rebuildXml(5);
    }

    /**
     * Removes the task from the task hopper.
     *
     * Uses delete.xq.
     *
     * @return true if removed properly from database, false otherwise.
     */
    public boolean removeFromHopper() throws ConnectionNotFoundException {
        boolean out = false;

        try {
<<<<<<< HEAD
            Document xmlResp = Task.connection.
                    loadDocument("delete.xq?id=" + this.getId(), null);
=======
            Document xmlResp = Task.getConnection().
                    loadDocument(ApplicationSettings.xquery.get("delete") +
                        "?id=" + this.getId(), null);

>>>>>>> 967aa1b... removing checkConnection/hasConnection from Task
            if (xmlResp.getRootElement().getChildElements().size() == 0) {
                out = true;
            }
        } catch (IOException ex) {
            Task.log(ex);
        } catch (SAXException ex) {
            Task.log(ex);
        } catch (ParsingException ex) {
            Task.log(ex);
        }

        return out;
    }

    /**
     * Returns a description of the task object as a <code>JSONObject</code>.
     *
     * Within the JSON object, the URIs of the graph, issue and task type are present as well
     * as the URI of the property. Example data, if available, is also included, as is the
     * broken value and details about the dataset.
     *
     * @return A JSONObject representing the task.
     */
    public JSONObject toJSON()
            throws ParsingException, IOException, SAXException, URISyntaxException, JSONException {

        JSONObject json = new JSONObject();
        json.put("id", this.getId());
        json.put("graphUri", this.getGraphUri());
        json.put("issueUri", this.getIssueUri());
        json.put("taskType", this.getTaskType());
        json.put("property", this.getIssuePredicate());
        json.put("dataset", this.getDataset().toMap());
        json.put("example", this.getExampleData(5));
        json.put("brokenValue", this.getIssueValuesMap());

        return json;
    }

    /**
     * Returns task as XML.
     *
     * @return Task as XML document.
     */
    public Document toXML() {
        if (this.xml != null) {
            return this.xml;
        } else {
            Element elem = new Element("rsp");
            Element empty = new Element("empty");
            elem.appendChild(empty);
            return new Document(elem);
        }
    }

    @Override
    public String toString() {
        String out = "Task: ";
        out = out + this.getTaskType() + " | ";
        out = out + this.getIssueUri() + " | ";
        out = out + this.getGraphUri();
        return out;
    }
}
