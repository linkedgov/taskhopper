package org.linkedgov.taskhopper;
import groovyx.net.http.URIBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import nu.xom.ParsingException;
import org.xml.sax.SAXException;
import nu.xom.Document;

public class TaskSelector {
    // <editor-fold defaultstate="collapsed" desc="Connection conn;">

    private Connection connection;
     /**
     * @return the connection to the database
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @param conn the connection to the database to set
     */
    public void setConnection(Connection conn) {
        this.connection = conn;
    }// </editor-fold>

    public TaskSelector(String address, Integer port) {
        if (address == null) {
            throw new NullPointerException("Address must be set.");
        }
        if (port == null) {
            port = 8080;
        }
        Connection newConn = new Connection(address, port);
        this.connection = newConn;
    }

    public TaskSelector(Connection conn) {
        this.connection = conn;
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
    public Document byId(String taskId) throws IOException, SAXException, ParsingException {
        Document xml = this.getConnection().loadDocument("get.xq?id=" + taskId);
        return xml;
    }

    /**
     * Ges a random task from the database.
     *
     * @return Document
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public Document random() throws IOException, SAXException, ParsingException {
        Document xml = this.getConnection().loadDocument("get_random.xq");
        return xml;
    }

    /**
     * Gets a random task by type
     *
     * @param type The type ID (e.g. "spelling-error").
     * @return Document
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public Document randomByType(String type)
            throws IOException, SAXException, ParsingException,
            URISyntaxException, URISyntaxException {
        URIBuilder uri = new URIBuilder("random_by_type.xq");
        uri.addQueryParam("type", type);
        Document xml = this.getConnection().loadDocument(uri);
        return xml;
    }

    /**
     * Gets all the tasks from the database.
     *
     * @return Document
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public Document all() throws IOException, SAXException, ParsingException {
        Document xml = this.getConnection().loadDocument("get.xq");
        return xml;
    }

    /**
     * Gets tasks sorted by priority.
     *
     * @return Document
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public Document priority() throws IOException, SAXException, ParsingException {
        Document xml = this.getConnection().loadDocument("priority.xq");
        return xml;
    }

    /**
     * Creates a task in the database.
     *
     * @param taskType
     * @param issueUri
     * @param graphUri
     * @param id Leave this null to have an ID assigned automatically. If you
     * provide a URI that duplicates one in the database already, it will be
     * ignored and a new ID assigned.
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     * @throws URISyntaxException
     */
    public Document create(String taskType, String issueUri, String graphUri, String id)
            throws IOException, SAXException, ParsingException, URISyntaxException {
        Map<String, String> params = new HashMap<String, String>();
        if (issueUri != null && issueUri.length() > 0) {
            params.put("issue-uri", issueUri);
        }
        if (taskType != null && taskType.length() > 0) {
            params.put("task-type", taskType);
        }
        if (graphUri != null && graphUri.length() > 0) {
            params.put("graph-uri", graphUri);
        }
        if (id != null && id.length() > 0) {
            params.put("id", id);
        }
        URIBuilder uri = new URIBuilder("new.xq");
        uri.addQueryParams(params);
        System.out.println(uri.toURI().toString());
        Document xml = this.getConnection().loadDocument(uri);
        return xml;
    }

}
