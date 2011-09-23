package org.linkedgov.taskhopper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import org.linkedgov.taskhopper.thirdparty.URIBuilder;
import org.xml.sax.SAXException;

public class Dataset {
    // <editor-fold defaultstate="collapsed" desc="String title;">

    private String title;

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    } // </editor-fold>
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
    } // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="String id;">
    private String id;

    /**
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the ID of the dataset
     */
    public void setId(String id) {
        this.id = id;
    } // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Connection connection;">
    private Connection connection;

    public void setConnection(Connection conn) {
        this.connection = conn;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public boolean hasConnection() {
        if (this.getConnection() == null) {
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
    private void checkConnection() throws ConnectionNotFoundException {
        if (this.getConnection() == null) {
            throw new ConnectionNotFoundException(
                    "You need to set an connection by calling this.setConnection()");
        }
    }
    // </editor-fold>

    public Dataset(String title, String url, String id) {
        this.setTitle(title);
        this.setUrl(url);
        this.setId(id);
    }

    public Dataset() {
    }

    /**
     * Returns a HashMap of the dataset object, suitable for JSON output
     * with JSON.simple.
     *
     * @return HashMap with title and URL of dataset.
     */
    public Map<String, String> toMap() {
        Map<String, String> output = new HashMap<String, String>();
        if (this.getTitle() != null) {
            output.put("title", this.getTitle());
        }
        if (this.getUrl() != null) {
            output.put("url", this.getUrl());
        }
        if (this.getId() != null) {
            output.put("id", this.getId());
        }
        return output;
    }

    private int total = 0;
    public int getTotal() {
        return this.total;
    }

    public ArrayList<String> getInstanceListing(int start, int limit) throws URISyntaxException, ParsingException, IOException, SAXException {
        URIBuilder builder = new URIBuilder("http://localhost:8080/");
        builder.setHost(this.getConnection().getUrl());
        builder.setPort(this.getConnection().getPort());
        builder.setPath("/exist/rest/db/linkedgov-meta/taskhopper/paged_item_query.xq");
        builder.addQueryParam("collection", this.getId());
        builder.addQueryParam("start", start);
        builder.addQueryParam("limit", limit);
        Document doc = this.getConnection().loadUrl(builder.toURI().toString());
        int count =
                Integer.parseInt(doc.getRootElement().getAttribute("count").getValue());
        this.total = count;
        Nodes resources = doc.query("/rsp/li");

        ArrayList<String> out = new ArrayList<String>();
        for (int i = 0; i < resources.size(); i++) {
            Element elem = (Element) resources.get(i);
            String content = elem.getValue();
            out.add(content);
        }
        return out;
    }

    public ArrayList<String> getExampleData(String property, int limit)
            throws URISyntaxException, ParsingException, IOException, SAXException {
        int foundValues = 0;
        int docsProcessed = 0;
        HashSet set = new HashSet<String>();
        while (foundValues < limit) {
            int callLimit = 20;
            if (limit < 20) {
                callLimit = limit;
            }
            ArrayList<String> apiResults = this.getInstanceListing(docsProcessed, callLimit);
            for (String result : apiResults) {
                URIBuilder builder = new URIBuilder("http://localhost:8080/");
                builder.setHost(this.getConnection().getUrl());
                builder.setPort(this.getConnection().getPort());
                builder.setPath("/exist/rest" + result);
                Document xml = this.getConnection().loadUrl(builder.toURI().toString());
                Model model = TaskUpdater.getMainGraphFromDocument(xml);
                Property p = model.createProperty(property);
                StmtIterator stmts = model.listStatements((Resource) null, p, (RDFNode) null);
                while (stmts.hasNext()) {
                    Statement stmt = (Statement) stmts.next();
                    if (stmt.getObject().isLiteral()) {
                        String value = stmt.getLiteral().getLexicalForm();
                        set.add(value);
                        foundValues += 1;
                    }
                    stmt.getObject().isLiteral();
                }
                docsProcessed += 1;
            }
            if (docsProcessed == this.total) {
                break;
            }
        }

        // Convert set to list so we can sort the list by the lexical values.
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(set);
        java.util.Collections.sort(list);
        return list;
    }
}
