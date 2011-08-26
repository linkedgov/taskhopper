package org.linkedgov.taskhopper;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.linkedgov.taskhopper.support.NullResolver;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TaskSelector {

    private String address;
    private int port;
    private HttpClient client;

    public TaskSelector(String address, int port) {
        this.address = address;
        this.port = port;
        this.client = new DefaultHttpClient();
    }

    public Document byId(int taskId) throws IOException, ParserConfigurationException, SAXException {
        Document xml = this.loadDocument("get.xq?id=" + Integer.toString(taskId));
        return xml;
    }

    public Document random() throws IOException, ParserConfigurationException, SAXException {
        Document xml = this.loadDocument("get_random.xq");
        return xml;
    }

    public Document randomByType(String type) throws IOException, ParserConfigurationException, SAXException {
        Document xml = this.loadDocument("random_by_type.xq?type=" + type);
        return xml;
    }

    public Document all() throws IOException, ParserConfigurationException, SAXException {
        Document xml = this.loadDocument("get.xq");
        return xml;
    }

    public Document priority() throws IOException, ParserConfigurationException, SAXException {
        Document xml = this.loadDocument("priority.xq");
        return xml;
    }

//    public void create() throws IOException, ParserConfigurationException, SAXException {
//
//    }
    
    private Document loadDocument(String url) throws IOException, ParserConfigurationException, SAXException {
        HttpGet get = new HttpGet("http://" + this.address + ":" + this.port +
                "/exist/rest/db/linkedgov-meta/taskhopper/" + url);
        HttpResponse response = this.client.execute(get);
        HttpEntity entity = response.getEntity();
        Document xml = TaskSelector.readDocument(entity.getContent());
        return xml;
    }

    private static Document readDocument(InputStream is)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setNamespaceAware(true);

        DocumentBuilder db = null;
        db = dbf.newDocumentBuilder();
        db.setEntityResolver(new NullResolver());

        return db.parse(is);
    }
}
