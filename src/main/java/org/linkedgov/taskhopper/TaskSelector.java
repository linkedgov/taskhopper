package org.linkedgov.taskhopper;
import com.hp.hpl.jena.rdf.model.*;
import org.linkedgov.taskhopper.http.ApplicationSettings;
import org.linkedgov.taskhopper.support.RDFToXOM;
import org.linkedgov.taskhopper.thirdparty.URIBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import nu.xom.ParsingException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import org.xml.sax.SAXException;
import java.util.ArrayList;

/**
 * Wraps access to tasks in the database.
 *
 * @author tom
 */
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

    /**
     * Creates a <code>TaskSelector</code> and a new <code>Connection</code>.
     *
     * @param address The address of the eXist server (e.g. localhost)
     * @param port The port number of the eXist server (e.g. 8080)
     */
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

    /**
     * Creates a TaskSelector from an existing <code>Connection</code>.
     *
     * @param conn <code>Connection</code> to an eXist server.
     */
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
        Document xml = this.getConnection().loadDocument("get.xq?id=" + taskId, null);
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
        Document xml = this.getConnection().loadDocument("get_random.xq", null);
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
        Document xml = this.getConnection().loadDocument("get.xq", null);
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
        Document xml = this.getConnection().loadDocument(uri);
        return xml;
    }

    /**
     * Retrieves a document from the XML database (or anywhere with a URI),
     * parses the issues from it and returns an array of Task objects for
     * storage in the database.
     *
     * @param url
     * @throws ParsingException
     * @throws IOException
     * @throws DataFormatException
     */
    public ArrayList<Task> importIssues(String url)
            throws ParsingException, IOException {
        Document xml = this.getConnection().loadUrl(url);

        /* Parse the main document from the XML into RDF. */
        Element root = xml.getRootElement();
        Element mainDoc = root.getFirstChildElement("main");
        Element mainDocRDF = mainDoc.getFirstChildElement("RDF",
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        Model model = RDFToXOM.convertFromXOM(mainDocRDF);
        /* Now select "potentiallyIncorrect" property. */
        Property incorrect = model.createProperty(ApplicationSettings.potentiallyIncorrectUri);
        StmtIterator stmts = model.listStatements((Resource) null, incorrect, (Resource) null);
        ArrayList<Task> tasks = new ArrayList<Task>();

        while(stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            String issueUri = stmt.getObject().toString();

            /* for each issue, we need to retrieve the issue type */
            String issueUriXpath = String.format("issue[@uri = '%s']", issueUri);
            Nodes issuesByUri = root.query(issueUriXpath);

            if (issuesByUri.size() > 0) {
                Element issue = (Element) issuesByUri.get(0);
                String taskType = issue.getAttribute("task-type").getValue();
                Task t = new Task(taskType, issueUri, url);
                tasks.add(t);
            }
        }

        return tasks;
    }
}
