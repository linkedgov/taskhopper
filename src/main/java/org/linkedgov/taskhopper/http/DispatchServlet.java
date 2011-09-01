package org.linkedgov.taskhopper.http;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DispatchServlet extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String uri = request.getRequestURI();
        
        if (uri.matches("^/task/new")) {
            InputServlet srv = new InputServlet();
            srv.processRequest(request, response);
        } else if (uri.matches("^/task/random")) {
            OutputServlet srv = new OutputServlet();
            request.setAttribute("action", "random");
            srv.processRequest(request, response);
        } else if (uri.matches("^/task/\\d+")) {
            OutputServlet srv = new OutputServlet();
            request.setAttribute("action", "byId");
            // parse out ID
            String reqId = uri.replaceAll("/task/(\\d+)", "$1");
            request.setAttribute("id", reqId);
            srv.processRequest(request, response);
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
    /**
     * Takes a path array, turns it into an <code>ArrayList</code> and removes
     * empty strings.
     *
     * @param path
     * @return an ArrayList of path elements.
     */
    public static ArrayList<String> convertArrayAndRemoveEmpty(String[] path) {
        ArrayList<String> output = new ArrayList<String>();
        for (String elem : path) {
            if (!elem.isEmpty()) {
                output.add(elem);
            }
        }
        return output;
    }
}
