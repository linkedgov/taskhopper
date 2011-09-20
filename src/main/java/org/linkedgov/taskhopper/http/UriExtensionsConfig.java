package org.linkedgov.taskhopper.http;
import java.util.*;
import javax.ws.rs.core.*;
import com.sun.jersey.api.core.*;

/**
 * Defines media types provided by the TaskHopper.
 *
 * @author tom
 */
public class UriExtensionsConfig extends PackagesResourceConfig {
    private Map<String, MediaType> mediaTypeMap;

    public UriExtensionsConfig()
    {
        super();
    }

    public UriExtensionsConfig(Map<String, Object> props)
    {
        super(props);
    }

    public UriExtensionsConfig(String[] paths)
    {
        super(paths);
    }

    @Override
    public Map<String, MediaType> getMediaTypeMappings()
    {
        if (mediaTypeMap == null) {
            mediaTypeMap = new HashMap<String, MediaType>();
            mediaTypeMap.put("json", MediaType.APPLICATION_JSON_TYPE);
            mediaTypeMap.put("xml", MediaType.APPLICATION_XML_TYPE);
            mediaTypeMap.put("js", new MediaType("application", "javascript"));
        }
        return mediaTypeMap;
    }
}
