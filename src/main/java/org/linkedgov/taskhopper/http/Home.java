package org.linkedgov.taskhopper.http;

import java.util.Set;
import java.util.HashSet;
import javax.ws.rs.core.Application;

public class Home extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(ById.class);
        s.add(Random.class);
        return s;
    }
}
