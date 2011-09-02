package org.linkedgov.taskhopper;
import com.hp.hpl.jena.rdf.model.*;
import groovyx.net.http.URIBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import nu.xom.ParsingException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import org.xml.sax.SAXException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

public class TaskSelector {
    private final String potentiallyIncorrectUri =
            "http://linkedgov.org/schema/potentiallyIncorrect";
    
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
     * Gets tasks sorted by priority.
     *
     * @return Document
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public Document priority() throws IOException, SAXException, ParsingException {
        Document xml = this.getConnection().loadDocument("priority.xq", null);
        return xml;
    }
}
