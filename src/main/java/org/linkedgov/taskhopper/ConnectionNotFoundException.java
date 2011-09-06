package org.linkedgov.taskhopper;
import java.io.IOException;

public class ConnectionNotFoundException extends IOException {

    /**
     * Creates a new instance of <code>ConnectionNotFoundException</code> without detail message.
     */
    public ConnectionNotFoundException() {
    }


    /**
     * Constructs an instance of <code>ConnectionNotFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ConnectionNotFoundException(String msg) {
        super(msg);
    }
}
