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
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import org.linkedgov.taskhopper.thirdparty.URIBuilder;
import org.xml.sax.SAXException;

/**
 * Hold information about datasets and extract metadata from database.
 *
 * @author tom
 */
public class Dataset {
    // <editor-fold defaultstate="collapsed" desc="String title;">

    private String title;

    /**
     * @return the title of the dataset (e.g. "Schools")
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set the dataset to
     */
    public void setTitle(String title) {
        this.title = title;
    } // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="String url;">
    private String url;

    /**
     * @return the URL of the dataset (e.g. "http://data.linkedgov.org/schools")
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the URL to set the dataset to
     */
    public void setUrl(String url) {
        this.url = url;
    } // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="String id;">
    private String id;

    /**
     * @return the ID of the dataset (e.g. "schools")
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the ID to set the dataset to
     */
    public void setId(String id) {
        this.id = id;
    } // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Connection connection;">
    private Connection connection;
    
    /**
     * @param conn The <code>Connection</code> to the eXist server.
     */
    public void setConnection(Connection conn) {
        this.connection = conn;
    }
    
    /**
     * @return The <code>Connection</code> to the eXist server.
     */
    public Connection getConnection() {
        return this.connection;
    }
    
    /**
     * @return True if the connection is set, false otherwise.
     */
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
     * 
     * @throws ConnectionNotFoundException
     */
    private void checkConnection() throws ConnectionNotFoundException {
        if (this.getConnection() == null) {
            throw new ConnectionNotFoundException(
                    "You need to set an connection by calling this.setConnection()");
        }
    }
    // </editor-fold>
    
    /**
     * @param title The title of the dataset (e.g. "Schools")
     * @param url The URL of the dataset (e.g. "http://data.linkedgov.org/schools") which lists all the instances.
     * @param id The short ID of the dataset (e.g. "schools")
     */
    public Dataset(String title, String url, String id) {
        this.setTitle(title);
        this.setUrl(url);
        this.setId(id);
    }

    public Dataset() {
    }

    /**
     * Returns a HashMap of the dataset object, suitable for JSON output
     * with JSON output.
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

    /**
     * Holds the number of instances in this dataset.
     */
    private int cachedInstanceCount = 0;

    /**
     * Whether or not the instance count has been loaded from the database.
     */
    private boolean cachedInstanceCountLoaded = false;
    
    /**
     * @return Number of instances in the dataset.
     */
    public int getInstanceCount() {
        if (this.cachedInstanceCountLoaded == false) {
            return this.loadInstanceCount();
        } else {
            return this.cachedInstanceCount;
        }
    }

    /**
     * Loads the instance count from the database.
     *
     * @return int the number of instances in the dataset.
     */
    public int loadInstanceCount() {
        int out = 0;

        try {
            // TODO: get rid of hard-coded URLs throughout
            URIBuilder builder = new URIBuilder("http://localhost:8080/");
            builder.setHost(this.getConnection().getUrl());
            builder.setPort(this.getConnection().getPort());
            // TODO: put XQueries into config file
            builder.setPath("/exist/rest/db/linkedgov-meta/taskhopper/instance_count.xq");
            builder.addQueryParam("collection", this.getId());
            Document doc = this.getConnection().loadUrl(builder.toURI().toString());
            out =
                Integer.parseInt(doc.getRootElement().getAttribute("count").getValue());
            this.cachedInstanceCount = out;
            this.cachedInstanceCountLoaded = true;
        } catch (ParsingException ex) {
            Logger.getLogger(Dataset.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Dataset.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Dataset.class.getName()).log(Level.SEVERE, null, ex);
        }

        return out;
    }

    /**
     * Gets a list of instances in a dataset (up to the <code>limit</code>).
     *
     * @param start The number of records to skip (equivalent to OFFSET in SQL).
     * @param limit The total number of records to retrieve
     * @return array of URLs of instances.
     * @throws URISyntaxException
     * @throws ParsingException
     * @throws IOException
     * @throws SAXException
     */
    public ArrayList<String> getInstanceListing(int start, int limit) throws URISyntaxException, ParsingException, IOException, SAXException {
        /* Construct URL. */
        URIBuilder builder = new URIBuilder("http://localhost:8080/");
        builder.setHost(this.getConnection().getUrl());
        builder.setPort(this.getConnection().getPort());
        builder.setPath("/exist/rest/db/linkedgov-meta/taskhopper/paged_item_query.xq");
        builder.addQueryParam("collection", this.getId());
        builder.addQueryParam("start", start);
        builder.addQueryParam("limit", limit);

        /* Retrieve data from the database. */
        Document doc = this.getConnection().loadUrl(builder.toURI().toString());
        int count =
                Integer.parseInt(doc.getRootElement().getAttribute("count").getValue());
        this.cachedInstanceCount = count;
        Nodes resources = doc.query("/rsp/li");

        /* Parse the data from the XML and output as an array. */
        ArrayList<String> out = new ArrayList<String>();
        for (int i = 0; i < resources.size(); i++) {
            Element elem = (Element) resources.get(i);
            String content = elem.getValue();
            out.add(content);
        }
        return out;
    }

    /**
     * Get valid example data from instances in the database up to a certain <code>limit</code>.
     *
     * @param property The URI of the property you wish to retrieve.
     * @param limit Total number of examples to retrieve.
     * @return list of example strings
     * @throws URISyntaxException
     * @throws ParsingException
     * @throws IOException
     * @throws SAXException
     */
    public ArrayList<String> getExampleData(String property, int limit)
            throws URISyntaxException, ParsingException, IOException, SAXException {
        int foundValues = 0;
        int docsProcessed = 0;
        HashSet set = new HashSet<String>();
        while (foundValues <= limit) {
            int callLimit = 20;
            if (limit < 20) {
                callLimit = limit;
            }
            int max = Math.max(docsProcessed, foundValues);
            int start = 0;
            if (docsProcessed != 0) {
                start = docsProcessed + 1;
            }
            ArrayList<String> apiResults = this.getInstanceListing(start, callLimit);
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
                }
                docsProcessed += 1;
            }
            if (docsProcessed == this.cachedInstanceCount) {
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
