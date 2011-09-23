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
    // <editor-fold defaultstate="collapsed" desc="DefaultHttpClient client;">
    private DefaultHttpClient client;

    /**
     * @return the client
     */
    public HttpClient getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(DefaultHttpClient client) {
        this.client = client;
    }//</editor-fold>
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
        this.authenticationCallback();
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
        this.authenticationCallback();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="boolean authenticating;">
    private boolean authenticating;

    /**
     * @return whether the HTTP request are being authenticated 
     */
    public boolean isAuthenticating() {
        return authenticating;
    }

    /**
     * @param authenticating the authenticating to set
     */
    public void setAuthenticating(boolean authenticating) {
        this.authenticating = authenticating;
    } //</editor-fold>

    /**
     * authenticationCallback is called after the username or password
     * is updated. If both username and password have been set, it sets
     * the credentials with the HTTP Client (this.client) and modifies
     * the authenticating method to true.
     *
     * Note: this.authenticating and the authentication callback
     * don't check to see if the username and password work, but
     * this is for database access rather than for use by end users.
     */
    private void authenticationCallback() {
        if (this.username != null && this.password != null) {
            this.client.getCredentialsProvider().setCredentials(
                    new AuthScope(this.url, this.port),
                    new UsernamePasswordCredentials(this.username, this.password));
            this.setAuthenticating(true);
        } else {
            this.setAuthenticating(false);
        }
    }

    // TODO: javaDoc this method
    public Connection(String url, Integer port) {
        this.setUrl(url);
        this.setPort(port);
        this.client = new DefaultHttpClient();
        this.setAuthenticating(false);
    }

    // TODO: javaDoc this method
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

    public boolean putDocument(Document xml, String url)
            throws UnsupportedEncodingException, IOException, ParsingException {
        HttpPut put = new HttpPut(url);
        put.addHeader("Content-Type", "application/xml");
        StringEntity ent = new StringEntity(xml.toXML());
        put.setEntity(ent);
        HttpResponse response = this.client.execute(put);
        if (response.getStatusLine().getStatusCode() == 201) {
            return true;
        } else {
            return false;
        }
    }
}
