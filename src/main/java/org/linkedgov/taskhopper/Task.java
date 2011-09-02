package org.linkedgov.taskhopper;

import org.linkedgov.taskhopper.thirdparty.URIBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import nu.xom.Document;
import nu.xom.ParsingException;
import org.xml.sax.SAXException;

public class Task {
    // <editor-fold defaultstate="collapsed" desc="String id;">
    private String id;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="String taskType;">
    private String taskType;

    /**
     * @return the taskType
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * @param taskType the taskType to set
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="String issueUri;">
    private String issueUri;
    /**
     * @return the issueUri
     */
    public String getIssueUri() {
        return issueUri;
    }

    /**
     * @param issueUri the issueUri to set
     */
    public void setIssueUri(String issueUri) {
        this.issueUri = issueUri;
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="String graphUri;">
    private String graphUri;
    /**
     * @return the graphUri
     */
    public String getGraphUri() {
        return graphUri;
    }

    /**
     * @param graphUri the graphUri to set
     */
    public void setGraphUri(String graphUri) {
        this.graphUri = graphUri;
    }// </editor-fold>

    private boolean inDatabase = false;

    public Task(String taskType, String issueUri, String graphUri) {
        if (taskType != null) {
            this.setTaskType(taskType);
        }
        if (issueUri != null) {
            this.setIssueUri(issueUri);
        }
        if (graphUri != null) {
            this.setGraphUri(graphUri);
        }
    }

    public void update(Connection conn) {
        // TODO: implement update
    }
    
    public Document create(Connection conn)
            throws URISyntaxException, IOException, SAXException, ParsingException {
        Map<String, String> params = new HashMap<String, String>();
        if (this.getIssueUri() != null && this.getIssueUri().length() > 0) {
            params.put("issue-uri", this.getIssueUri());
        }
        if (this.getTaskType() != null && this.getTaskType().length() > 0) {
            params.put("task-type", this.getTaskType());
        }
        if (this.getGraphUri() != null && this.getGraphUri().length() > 0) {
            params.put("graph-uri", this.getGraphUri());
        }
        if (this.id != null && this.id.length() > 0) {
            params.put("id", this.id);
        }
        
        URIBuilder uri = new URIBuilder("new.xq");
        uri.addQueryParams(params);
        Document xml = conn.loadDocument(uri);
        return xml;
    }

    public void save(Connection conn)
            throws URISyntaxException, IOException, SAXException, ParsingException {
        if (this.inDatabase == true) {
            this.update(conn);
        } else {
            this.create(conn);
        }
    }

    @Override
    public String toString() {
        String out = "Task: ";
        out = out + this.getTaskType() + " | ";
        out = out + this.getIssueUri() + " | ";
        out = out + this.getGraphUri();
        return out;
    }

}
