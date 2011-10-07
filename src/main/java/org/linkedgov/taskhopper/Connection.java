package org.linkedgov.taskhopper;

import java.io.UnsupportedEncodingException;
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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.xml.sax.SAXException;

/**
 * Stores details for and provides convenience methods for connecting to the eXist database.
 *
 * @author tom
 */
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
    // <editor-fold defaultstate="collapsed" desc="String username;">
    private String username;

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    } // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="String password;">
    private String password;

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    // </editor-fold>

    /**
     * Creates and returns a new <code>DefaultHttpClient</code> using the current settings (authentication etc.).
     *
     * @return HttpClient
     */
    public HttpClient getClient() {
        DefaultHttpClient http = new DefaultHttpClient();
        if (this.username != null && this.password != null) {
            http.getCredentialsProvider().setCredentials(
                    new AuthScope(this.url, this.port),
                    new UsernamePasswordCredentials(this.username, this.password));
        }
        return http;
    }

    /**
     * Create <code>Connection</code> from address and port.
     *
     * @param address The hostname of the eXist server (e.g. localhost).
     * @param port The port number of the eXist server (e.g. 8080).
     */
    public Connection(String address, Integer port) {
        this.setUrl(address);
        this.setPort(port);
    }

    /**
     * Loads XML from a URL and returns it as a XOM <code>Document</code> object.
     *
     * @param url A valid URL (e.g. "http://example.org")
     * @return A XOM <code>Document</code> object containing the document loaded.
     */
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
    
    /**
     * Puts an XML <code>Document</code> into the database.
     *
     * @param xml A XOM <code>Document</code>.
     * @param url URL to PUT the document to.
     * @return boolean Whether the document was PUT without issue.
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws ParsingException
     */
    public boolean putDocument(Document xml, String url)
            throws UnsupportedEncodingException, IOException, ParsingException {
        HttpPut put = new HttpPut(url);
        put.addHeader("Content-Type", "application/xml");
        StringEntity ent = new StringEntity(xml.toXML());
        put.setEntity(ent);
        HttpResponse response = this.getClient().execute(put);
        if (response.getStatusLine().getStatusCode() == 201) {
            return true;
        } else {
            return false;
        }
    }
}
