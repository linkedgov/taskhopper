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
import org.linkedgov.taskhopper.TaskSelector;
import org.xml.sax.SAXException;

public class OutputServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // TODO: put the configuration into a properties file or into Maven etc.
        TaskSelector ts = new TaskSelector("localhost", 8080);
        response.setContentType("application/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        // TODO: test this
        System.out.println(request.getServletPath());

        try {
            Document dbResponse;
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
    /* TODO: implement this, and possibly extract, rename and turn into a static in a separate class */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // <editor-fold defaultstate="collapsed" desc="Not implemented yet.">
        response.setContentType("application/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write("<?xml version=\"1.0\" />\n");
        out.write("<error>Not implemented yet.</error>");
        processRequest(request, response);
        // </editor-fold>
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "For retrieving individual issues from the task hopper and updating the result.";
    }// </editor-fold>
}
