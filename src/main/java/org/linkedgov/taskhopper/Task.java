package org.linkedgov.taskhopper;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
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

public class Task {
    // <editor-fold defaultstate="collapsed" desc="String id;">

    private String id;

    /**
     * @return the id
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
     * @return the taskType
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * @param taskType the taskType to set
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="String issueUri;">
    private String issueUri;

    /**
     * @return the issueUri
     */
    public String getIssueUri() {
        return issueUri;
    }

    /**
     * @param issueUri the issueUri to set
     */
    public void setIssueUri(String issueUri) {
        this.issueUri = issueUri;
    }// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="String graphUri;">
    private String graphUri;

    /**
     * @return the graphUri
     */
    public String getGraphUri() {
        return graphUri;
    }

    /**
     * @param graphUri the graphUri to set
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

    public static Connection getConnection() {
        return Task.connection;
    }

    public static boolean hasConnection() {
        if (Task.getConnection() == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks to see if a connection is available.
     * If not, throw <code>ConectionNotFoundException</code>.
     * @throws ConnectionNotFoundException
     */
    private static void checkConnection() throws ConnectionNotFoundException {
        if (Task.getConnection() == null) {
            throw new ConnectionNotFoundException(
                    "You need to set an connection by calling Task.setConnection()");
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
        Task.checkConnection();
        Document xml = Task.getConnection().loadDocument("get.xq?id=" + taskId, null);
        Task t = Task.xmlToTask(xml);
        t.rebuildXml(5);
        return t;
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
    
    public static Document randomWrappedXml() {
        Document xml = new Document(new Element("rsp"));
        try {
            Task t = Task.random();
            if (t != null) {
                xml.appendChild(t.toXML());
            }
        } catch (IOException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParsingException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        }
        return xml;
    }
    
    public static JSONArray randomWrappedJSON() {
        JSONArray json = new JSONArray();
        Task t;
        try {
            t = Task.random();
            if (t != null) {
                json.put(t.toJSON());
            }
        } catch (IOException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParsingException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json;
    }

    public static Task randomByType(String type)
            throws IOException, SAXException, ParsingException, URISyntaxException {
        URIBuilder uri = new URIBuilder("random_by_type.xq");
        uri.addQueryParam("type", type);
        Document xml = Task.connection.loadDocument(uri);
        if (xml.getRootElement().getChildElements().size() == 0) {
            return null;
        } else {
            Task t = Task.xmlToTask(xml);
            t.xml = xml;
            t.rebuildXml(5);
            return t;
        }
    }

    public static Document randomByTypeWrappedXml(String type)
    {
        Document xml = new Document(new Element("rsp"));
        try {
            Task t = Task.randomByType(type);
            xml.appendChild(t.toXML());
        } catch (IOException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParsingException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        }
        return xml;
    }

    public static JSONArray randomByTypeWrappedJson(String type)
    {
        JSONArray json = new JSONArray();
        try {
            Task t = Task.randomByType(type);
            json.put(t.toJSON());
        } catch (IOException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParsingException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json;
    }

    /**
     * Turn XML task document into Task.
     *
     * @param xml
     * @return Task instance.
     */
    public static Task xmlToTask(Document xml) {
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

        if (this.id != null && this.id.length() > 0) {
            params.put("id", this.id);
        }

        URIBuilder uri = new URIBuilder("new.xq");
        uri.addQueryParams(params);
        Document xmlResp = Task.connection.loadDocument(uri);
        return xmlResp;
    }
    
    /**
     * Calls TaskUpdater.nullifyTask to mark a task as nullified.
     *
     * @return Document containing the updated graph.
     * @throws ParsingException
     * @throws IOException
     */
    public Document nullify()
            throws ParsingException, IOException {
        Task.checkConnection();
        Document input = Task.getConnection().loadUrl(this.getGraphUri());
        Document output = TaskUpdater.nullifyTask(input, this.getIssueUri());
        boolean resp = Task.connection.putDocument(output, this.getGraphUri());
        if (resp == true) {
            boolean removed = this.removeFromHopper();
        }
        return output;
    }

    /**
     * Calls TaskUpdater.markAsOkay to mark data as okay.
     *
     * @return
     * @throws ParsingException
     * @throws IOException
     */
    public Document okay()
            throws ParsingException, IOException {
        Task.checkConnection();
        Document input = Task.getConnection().loadUrl(this.getGraphUri());
        Document output = TaskUpdater.markAsOkay(input, this.getIssueUri());
        boolean resp = Task.connection.putDocument(output, this.getGraphUri());
        if (resp == true) {
            boolean removed = this.removeFromHopper();
        }
        return output;
    }

    public Document referToExpert()
            throws ParsingException, IOException, URISyntaxException, SAXException {
        Task.checkConnection();
        Document input = Task.getConnection().loadUrl(this.getGraphUri());
        Document output = TaskUpdater.referToExpert(input, this.getIssueUri());
        boolean resp = Task.connection.putDocument(output, this.getGraphUri());
        /* No need to call removeFromHopper as the XQuery to reimport the
         * task should do the job. And if it fails, it's probably safer to
         * have two tasks in the task list. */
        TaskSelector ts = new TaskSelector(Task.getConnection());
        ArrayList<Task> tasks = ts.importIssues(this.getGraphUri());
        for (Task task : tasks) {
           Logger.getLogger(Task.class.getName()).info(task.toString());
           task.create();
        }
        return output;
    }

    /**
     * Calls TaskUpdater.editValue to change the value in a graph.
     *
     * @param value
     * @return Document containing the updated graph.
     * @throws ParsingException
     * @throws IOException
     */
    public Document edit(String value)
            throws ParsingException, IOException {
        Task.checkConnection();
        Document input = Task.getConnection().loadUrl(this.getGraphUri());
        Document output = TaskUpdater.editValue(input, this.getIssueUri(), value, null);
        boolean resp = Task.connection.putDocument(output, this.getGraphUri());
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
            href = datasetElem.getAttribute("href");
            title = datasetElem.getAttribute("title");
            datasetId = datasetElem.getAttribute("id");
        }
        Dataset dataset = new Dataset();
        if (href != null) {
            dataset.setUrl(href.getValue());
        }
        if (title != null) {
            dataset.setTitle(title.getValue());
        }
        if (datasetId != null) {
            dataset.setId(datasetId.getValue());
        }
        dataset.setConnection(Task.getConnection());
        return dataset;
    }

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
        Model taskGraph = TaskUpdater.getTaskGraphFromDocument(xmlResp, this.getIssueUri());
        StmtIterator stmts = taskGraph.listStatements();
        Property predicate = null;
        while (stmts.hasNext()) {
            Statement stmt = (Statement) stmts.next();
            predicate = stmt.getPredicate();
        }
        return predicate.getURI();
    }

    public String getBrokenValue() throws ParsingException, IOException {
        Map<String, String> issueValues = this.getIssueValuesMap();
        return issueValues.get("value");
    }

    public Map<String, String> getIssueValuesMap() throws ParsingException, IOException {
        Map<String, String> out = new HashMap<String, String>();
        Document xmlResp = Task.getConnection().loadUrl(this.getGraphUri());
        Model taskGraph = TaskUpdater.getTaskGraphFromDocument(xmlResp, this.getIssueUri());
        StmtIterator stmts = taskGraph.listStatements();
        assert (taskGraph.size() == 1);
        String resp = null;
        String datatype = null;
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

    public void rebuildXml(int maximum) {
        Element root = new Element("task");
        root.addAttribute(new Attribute("id", this.getId()));

        if (this.getTaskType() != null) {
            Element taskTypeElem = new Element("task-type");
            taskTypeElem.addAttribute(new Attribute("task-type", this.getTaskType()));
            root.appendChild(taskTypeElem);
        }

        if (this.getGraphUri() != null) {
            Element graphUriElem = new Element("graph-uri");
            graphUriElem.addAttribute(new Attribute("graph-uri", this.getGraphUri()));
            root.appendChild(graphUriElem);
        }

        if (this.getIssueUri() != null) {
            Element issueUriElem = new Element("issue-uri");
            issueUriElem.addAttribute(new Attribute("issue-uri", this.getIssueUri()));
            root.appendChild(issueUriElem);
        }

        try {
            String issuePred = this.getIssuePredicate();
            if (issuePred != null) {
                Element propertyElem = new Element("property");
                propertyElem.addAttribute(new Attribute("property", issuePred));
                root.appendChild(propertyElem);
            }

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

        } catch (ParsingException e) {
            // Log
        } catch (IOException e) {
            // Log
        } catch (URISyntaxException e) {
            // Log
        } catch (SAXException e) {
            // Log
        }

        Document doc = new Document(root);
        this.xml = doc;
    }

    public void rebuildXml() {
        this.rebuildXml(5);
    }

    public boolean removeFromHopper() throws ConnectionNotFoundException {
        Task.checkConnection();
        boolean out = false;
        try {
            Document xmlResp = Task.connection.
                    loadDocument("delete.xq?id=" + this.getId(), null);
            if (xmlResp.getRootElement().getChildElements().size() == 0) {
                out = true;
            }
        } catch (IOException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParsingException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
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

    public Document toXML() {
        return this.xml;
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
