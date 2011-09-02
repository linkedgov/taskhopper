package org.linkedgov.taskhopper;

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

    // <editor-fold defaultstate="collapsed" desc="int priority;">
    private int priority;
    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }// </editor-fold>

    public Task(String taskType, String issueUri, String graphUri, Integer priority) {
        if (taskType != null) {
            this.setTaskType(taskType);
        }
        if (issueUri != null) {
            this.setIssueUri(issueUri);
        }
        if (graphUri != null) {
            this.setGraphUri(graphUri);
        }
        if (priority != null) {
            this.setPriority(priority);
        }
    }

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
        this.setPriority(0);
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
