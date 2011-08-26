package org.linkedgov.taskhopper.http;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TaskHopperServlet extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response = TaskHopperServlet.processRequestXml(request, response);
    }

    protected static HttpServletResponse processRequestXml(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        // Input
        String url = request.getHeader("url");
        String id = request.getHeader("id");
        String type = request.getHeader("type");

        // TODO: Store in XML database
        // linkedgov-meta/taskhopper/new.xq

        // TODO: Output
        response.setContentType("application/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        // TODO: replace with database call:
        boolean storedInTaskHopper = true;
        try {
            if (storedInTaskHopper == true) {
                out.write("<?xml version=\"1.0\" ?>\n");
                out.write("<TaskHopperResponse status=\"OK\">\n");
            } else {
                out.write("<?xml version=\"1.0 ?>\n");
                out.write("<TaskHopperResponse status=\"Problems!\" />\n");
            }
        } finally {
            out.close();
        }
        return response;
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
