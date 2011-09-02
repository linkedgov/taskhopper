package org.linkedgov.taskhopper;

import org.linkedgov.taskhopper.thirdparty.URIBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;

public class Connection {
    // <editor-fold defaultstate="collapsed" desc="String url;">

    private String url;

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Integer port;">
    private Integer port;

    /**
     * @return port number
     */
    public Integer getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(Integer port) {
        this.port = port;
    }// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="HttpClient client;">
    private HttpClient client;
    /**
     * @return the client
     */
    public HttpClient getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(HttpClient client) {
        this.client = client;
    }//</editor-fold>

    public Connection(String url, Integer port) {
        this.setUrl(url);
        this.setPort(port);
        this.client = new DefaultHttpClient();
    }

    public Document loadUrl(String url) throws ParsingException, IOException {
        HttpGet get = new HttpGet(url);
        HttpResponse response = this.getClient().execute(get);
        HttpEntity entity = response.getEntity();
        Document xml = Connection.readDocument(entity.getContent());
        return xml;
    }
    /**
     * Loads a document from the database and parses it into an <code>Document</code>.
     *
     * @param urlStub The filename of the URL stub (e.g. random.xq).
     * @return Document
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public Document loadDocument(String urlStub, String path)
            throws IOException, SAXException, ParsingException {
        if (path == null) {
            path = "/exist/rest/db/linkedgov-meta/taskhopper/";
        }
        HttpGet get = new HttpGet("http://" + this.getUrl() + ":"
                + this.getPort() + path + urlStub);
        HttpResponse response = this.getClient().execute(get);
        HttpEntity entity = response.getEntity();
        Document xml = Connection.readDocument(entity.getContent());
        return xml;
    }

    /**
     * Loads a document from the database and parses it into an <code>Document</code>.
     *
     * @param urlStub The filename of the URL stub (e.g. random.xq).
     * @return Document
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public Document loadDocument(URIBuilder urlStub)
            throws IOException, SAXException, ParsingException {
        return this.loadDocument(urlStub.toURI().toString(), null);
    }

    /**
     * Loads a document from the database and parses it into an <code>Document</code>.
     *
     * @param urlStub The filename of the URL stub (e.g. random.xq).
     * @return Document
     * @throws IOException
     * @throws SAXException
     * @throws ParsingException
     */
    public Document loadDocument(URI urlStub)
            throws IOException, SAXException, ParsingException {
        return this.loadDocument(urlStub.toString(), null);
    }

    /**
     * Reads a document from an <code>InputStream</code> into a <code>Document</code>.
     *
     * @param is The <code>InputStream</code> containing XML.
     * @return Document
     * @throws ParsingException
     * @throws IOException
     */
    public static Document readDocument(InputStream is) throws ParsingException, IOException {
        Builder parser = new Builder();
        Document doc = parser.build(is);
        return doc;
    }
}
