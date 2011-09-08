package org.linkedgov.taskhopper.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import org.linkedgov.taskhopper.Connection;
import org.linkedgov.taskhopper.Task;
import org.linkedgov.taskhopper.TaskSelector;
import org.xml.sax.SAXException;

public class ImportServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/xml;charset=UTF-8");
        String url = request.getParameter("url");
        String decodedUrl = URLDecoder.decode(url, "UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Connection conn = ApplicationSettings.getConnection();
            TaskSelector ts = new TaskSelector(conn);
            ArrayList<Task> tasks = ts.importIssues(decodedUrl);

            /* Setup variables for output. */
            Nodes outputTaskNodes = new Nodes();
            int successCount = 0;
            int failureCount = 0;

            /* Iterate through the tasks and store them in the database. */
            for (Task task : tasks) {
                Document resp = task.create();
                boolean success = resp.getRootElement().getChildElements().size() > 0;
                if (success) {
                    successCount++;
                    Nodes successfulTasks = resp.getRootElement().query("task");
                    for (int i = 0; i < successfulTasks.size(); i++) {
                        outputTaskNodes.append(successfulTasks.get(i));
                    }
                } else {
                    failureCount++;
                }
            }

            /* Format a response containing imported tasks and attributes to provide
             * success and failure counts. */
            Element root = new Element("rsp");
            root.addAttribute(new Attribute("action", "import"));
            root.addAttribute(new Attribute("success", Integer.toString(successCount)));
            root.addAttribute(new Attribute("failure", Integer.toString(failureCount)));
            for (int i = 0; i < outputTaskNodes.size(); i++) {
                root.appendChild(outputTaskNodes.get(i));
            }

            /* Write output. */
            Document outputDoc = new Document(root);
            out.write(outputDoc.toXML());
        } catch (URISyntaxException e) {
            response.sendError(response.SC_INTERNAL_SERVER_ERROR);
            out.write("<?xml version=\"1.0\" ?>\n");
            out.write("<error>URISyntaxException</error>");
        } catch (SAXException e) {
            response.sendError(response.SC_INTERNAL_SERVER_ERROR);
            out.write("<?xml version=\"1.0\" ?>\n");
            out.write("<error>SAXException</error>");
        } catch (ParsingException e) {
            // TODO: respond with error message
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
