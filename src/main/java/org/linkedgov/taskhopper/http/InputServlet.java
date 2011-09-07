package org.linkedgov.taskhopper.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nu.xom.Document;
import nu.xom.ParsingException;
import org.apache.commons.lang.StringUtils;
import org.linkedgov.taskhopper.Connection;
import org.linkedgov.taskhopper.Task;
import org.linkedgov.taskhopper.TaskSelector;
import org.xml.sax.SAXException;

public class InputServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean willStore = true;
        ArrayList<String> errors = new ArrayList<String>();

        Connection conn = ApplicationSettings.getConnection();
        Task.setConnection(conn);
        TaskSelector ts = new TaskSelector(conn);
        String issueUri = request.getParameter("issue-uri");
        if (issueUri == null || issueUri.isEmpty()) {
            willStore = false;
            errors.add("The issue URI has not been set.");
        }

        String graphUri = request.getParameter("graph-uri");
        if (graphUri == null || graphUri.isEmpty()) {
            willStore = false;
            errors.add("The graph URI has not been set.");
        }

        String taskType = request.getParameter("task-type");
        if (taskType == null || taskType.isEmpty()) {
            willStore = false;
            errors.add("The task type has not been set.");
        }

        response.setContentType("application/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // TODO: implement "lookup and see if ID is in use"
        // if not return an error.

        /* If there has been an error raised in input parsing,
         * respond with an error message. */
        if (willStore == false) {
            response.sendError(response.SC_FORBIDDEN); // 403
            out.write("<?xml version=\"1.0\" ?>\n");
            out.write("<error>");
            out.write("The data submitted has errors.\n");
            out.write(StringUtils.join(errors, "\n"));
            out.write("</error>");
            out.close();
        }

        try {
            // Write to XML database.
            Document dbResponse;
            try {
                Task task = new Task(taskType, issueUri, graphUri);
                dbResponse = task.create();
                dbResponse = ts.create(taskType, issueUri, graphUri, null);
                // Output
                out.write(dbResponse.toXML());
            } catch (SAXException e) {
                response.sendError(response.SC_INTERNAL_SERVER_ERROR);
                out.write("<?xml version=\"1.0\" ?>\n");
                out.write("<error>SAXException</error>");
            } catch (ParsingException e) {
                response.sendError(response.SC_INTERNAL_SERVER_ERROR);
                out.write("<?xml version=\"1.0\" ?>\n");
                out.write("<error>ParsingException</error>");
            } catch (URISyntaxException e) {
                response.sendError(response.SC_INTERNAL_SERVER_ERROR);
                out.write("<?xml version=\"1.0\" ?>\n");
                out.write("<error>URISyntaxException</error>");
            }
        } finally {
            out.close();
        }
        response.flushBuffer();
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
        return "For adding individual issues to the task hopper.";
    }// </editor-fold>
}
