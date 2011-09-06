package org.linkedgov.taskhopper;

import org.linkedgov.taskhopper.thirdparty.URIBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
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
    private static Task xmlToTask(Document xml) {
        Element root = xml.getRootElement();
        String taskType = root.getFirstChildElement("task-type").getAttribute("href").getValue();
        String graphUri = root.getFirstChildElement("graph-uri").getAttribute("href").getValue();
        String issueUri = root.getFirstChildElement("issue-uri").getAttribute("href").getValue();
        Task t = new Task(taskType, issueUri, graphUri);
        String id = root.getAttribute("id").getValue();
        t.setId(id);
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

    @Override
    public String toString() {
        String out = "Task: ";
        out = out + this.getTaskType() + " | ";
        out = out + this.getIssueUri() + " | ";
        out = out + this.getGraphUri();
        return out;
    }
}
