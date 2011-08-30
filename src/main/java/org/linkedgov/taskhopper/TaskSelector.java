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

    public Document byId(int taskId) throws IOException, SAXException, ParsingException {
        Document xml = this.loadDocument("get.xq?id=" + Integer.toString(taskId));
        return xml;
    }

    public Document random() throws IOException, SAXException, ParsingException {
        Document xml = this.loadDocument("get_random.xq");
        return xml;
    }

    public Document randomByType(String type) throws IOException, SAXException, ParsingException {
        Document xml = this.loadDocument("random_by_type.xq?type=" + type);
        return xml;
    }

    public Document all() throws IOException, SAXException, ParsingException {
        Document xml = this.loadDocument("get.xq");
        return xml;
    }

    public Document priority() throws IOException, SAXException, ParsingException {
        Document xml = this.loadDocument("priority.xq");
        return xml;
    }

//    public void create() throws IOException, SAXException {
//
//    }
    
    private nu.xom.Document loadDocument(String url) throws IOException, SAXException, ParsingException {
        HttpGet get = new HttpGet("http://" + this.address + ":" + this.port +
                "/exist/rest/db/linkedgov-meta/taskhopper/" + url);
        HttpResponse response = this.client.execute(get);
        HttpEntity entity = response.getEntity();
        Document xml = TaskSelector.readDocumentXom(entity.getContent());
        return xml;
    }

    private static nu.xom.Document readDocumentXom(InputStream is) throws ParsingException, IOException {
        Builder parser = new Builder();
        nu.xom.Document doc = parser.build(is);
        return doc;
    }
}
