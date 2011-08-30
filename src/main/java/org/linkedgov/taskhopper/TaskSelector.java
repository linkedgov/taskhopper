package org.linkedgov.taskhopper;
import java.io.IOException;
import java.io.InputStream;
import nu.xom.ParsingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;
import nu.xom.Builder;
import nu.xom.Document;

public class TaskSelector {

    private String address;
    private int port;
    private HttpClient client;

    public TaskSelector(String address, int port) {
        this.address = address;
        this.port = port;
        this.client = new DefaultHttpClient();
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
        Document xml = this.loadDocument("get.xq?id=" + taskId);
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
        Document xml = this.loadDocument("get_random.xq");
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
    public Document randomByType(String type) throws IOException, SAXException, ParsingException {
        Document xml = this.loadDocument("random_by_type.xq?type=" + type);
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
        Document xml = this.loadDocument("get.xq");
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
        Document xml = this.loadDocument("priority.xq");
        return xml;
    }

//    public void create() throws IOException, SAXException {
//
//    }

    /**
     * Loads a document from the database and parses it into an <code>Document</code>.
     *
     * @param urlStub The filename of the URL stub.
     * @return Document
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    private Document loadDocument(String urlStub) throws IOException, SAXException, ParsingException {
        HttpGet get = new HttpGet("http://" + this.address + ":" + this.port +
                "/exist/rest/db/linkedgov-meta/taskhopper/" + urlStub);
        HttpResponse response = this.client.execute(get);
        HttpEntity entity = response.getEntity();
        Document xml = TaskSelector.readDocument(entity.getContent());
        return xml;
    }

    /**
     * Reads a document from an <code>InputStream</code> into a <code>Document</code>.
     *
     * @param is The <code>InputStream</code> containing XML.
     * @return Document
     * @throws ParsingException
     * @throws IOException
     */
    private static Document readDocument(InputStream is) throws ParsingException, IOException {
        Builder parser = new Builder();
        Document doc = parser.build(is);
        return doc;
    }
}
