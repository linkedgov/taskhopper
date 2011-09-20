package org.linkedgov.taskhopper;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
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
     * @return Document
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public static Task byId(String taskId)
            throws IOException, SAXException, ParsingException, ConnectionNotFoundException {
        Task.checkConnection();
        Document xml = Task.getConnection().loadDocument("get.xq?id=" + taskId, null);
        Task t = Task.xmlToTask(xml);
        return t;
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
        return t;
    }

    public void update(Connection conn) {
        // TODO: implement update
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
        Document xml = Task.connection.loadDocument(uri);
        return xml;
    }

    /**
     * Saves the task. If it isn't already in the database, it'll create a new
     * record. If it is, it'll update the record.
     *
     * @throws URISyntaxException
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public void save(Connection conn)
            throws URISyntaxException, IOException, SAXException, ParsingException {
        Task.checkConnection();

        if (this.inDatabase == true) {
            this.update(conn);
        } else {
            this.create();
        }
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
        Document output = TaskUpdater.nullifyTask(input, this.getId());
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
        Document output = TaskUpdater.editValue(input, this.getTaskType(), value, null);
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
        Document xml = Task.getConnection().loadUrl(this.getGraphUri());
        Model taskGraph = TaskUpdater.getTaskGraphFromDocument(xml, this.getIssueUri());
        StmtIterator stmts = taskGraph.listStatements();
        assert (taskGraph.size() == 1);
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
        Document xml = Task.getConnection().loadUrl(this.getGraphUri());
        Model taskGraph = TaskUpdater.getTaskGraphFromDocument(xml, this.getIssueUri());
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

    // TODO: javaDoc this
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

    @Override
    public String toString() {
        String out = "Task: ";
        out = out + this.getTaskType() + " | ";
        out = out + this.getIssueUri() + " | ";
        out = out + this.getGraphUri();
        return out;
    }
}
