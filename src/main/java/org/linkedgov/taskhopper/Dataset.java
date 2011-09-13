package org.linkedgov.taskhopper;

import java.util.HashMap;
import java.util.Map;

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

    public Dataset(String title, String url) {
        this.setTitle(title);
        this.setUrl(url);
    }

    public Dataset() {}

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
            output.put("url", this.getTitle());
        }
        return output;
    }
}
