package org.linkedgov.taskhopper.http;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import org.linkedgov.taskhopper.Connection;

/**
 * Loads connection settings to database from web.xml upon startup.
 *
 * @author tom
 */
public class ApplicationSettings implements javax.servlet.ServletContextListener {
    public static String needsAnExpert =
            "http://linkedgov.org/schema/task-types/needs-an-expert";

    public static String potentiallyIncorrectUri =
            "http://linkedgov.org/schema/potentiallyIncorrect";

    // <editor-fold defaultstate="collapsed" desc="static String serverHostName;">
    private static String serverHostName = "localhost";

    /**
     * @return the serverHostName
     */
    public static String getServerHostName() {
        return serverHostName;
    }//</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="static int port;">
    private static int port = 8080;

    /**
     * @return the port
     */
    public static int getPort() {
        return port;
    } // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="static String username;">
    private static String username;

    /**
     * @return the username
     */
    public static String getUsername() {
        return username;
    } // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="static String password;">
    private static String password;

    /**
     * @return the password
     */
    public static String getPassword() {
        return password;
    } // </editor-fold>

    public static Connection getConnection() {
        if (ApplicationSettings.getServerHostName() != null) {
            Connection conn = new Connection(ApplicationSettings.getServerHostName(),
                    ApplicationSettings.getPort());

            if (ApplicationSettings.getUsername() != null &&
                    ApplicationSettings.getPassword() != null) {
                conn.setUsername(ApplicationSettings.getUsername());
                conn.setPassword(ApplicationSettings.getPassword());
            }

            return conn;
        } else {
            return null;
        }
    }

    public static boolean hasConnection() {
        try {
            if (ApplicationSettings.getServerHostName() != null) {
                return true;
            } else {
                return false;
            }
        } catch(NullPointerException e) { return false; }
    }

    public static void initialize() throws NamingException {
        javax.naming.Context ctx = new javax.naming.InitialContext();
        javax.naming.Context env = (Context) ctx.lookup("java:comp/env");
        ApplicationSettings.serverHostName = (String) env.lookup("exist-hostname");
        ApplicationSettings.port = (Integer) env.lookup("exist-port");
        ApplicationSettings.username = (String) env.lookup("exist-username");
        ApplicationSettings.password = (String) env.lookup("exist-password");
        ApplicationSettings.needsAnExpert = (String) env.lookup("uri.needs-an-expert");
        ApplicationSettings.potentiallyIncorrectUri = (String) env.lookup("uri.potentially-incorrect");
    }

    public void contextInitialized(ServletContextEvent sce) {
        try {
            ApplicationSettings.initialize();
        } catch (NamingException ex) {}
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
