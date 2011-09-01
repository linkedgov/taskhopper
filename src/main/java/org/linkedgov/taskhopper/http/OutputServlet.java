package org.linkedgov.taskhopper.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import org.linkedgov.taskhopper.TaskSelector;
import org.xml.sax.SAXException;

public class OutputServlet extends HttpServlet {
    // TODO: put the configuration into a properties file or into Maven etc.
    private TaskSelector ts = new TaskSelector("localhost", 8080);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * Handles random and byId lookups.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (request.getAttribute("action") ==  "random") {
                if (request.getParameter("type") != null &&
                        !(request.getParameter("type").isEmpty())) {
                    /* Handle random with type, using random_by_type.xq */
                    String typeInput = request.getParameter("type");
                    Document randomByType = ts.randomByType(typeInput);
                    out.write(randomByType.toXML());
                } else {
                    /* Handle random with random.xq */
                    Document random = ts.random();
                    out.write(random.toXML());
                }

            }
            if (request.getAttribute("action") == "byId") {
                /* Handle requests for specific tasks by ID using get.xq */
                String reqId = (String) request.getAttribute("id");
                Document doc = ts.byId(reqId);

                /* Check to see if the result is empty. If it is,
                 * we return 404 Not Found status code. */
                Element root = doc.getRootElement();
                Elements empties = root.getChildElements("empty");
                if (empties.size() != 0) {
                    response.setStatus(response.SC_NOT_FOUND);
                }
                out.write(doc.toXML());
            }
        /* Catch hopefully rare errors. */
        } catch (SAXException e) {
            response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
            out.write("<?xml version=\"1.0\" ?>\n");
            out.write("<error>SAXException</error>");
        } catch (ParsingException e) {
            response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
            out.write("<?xml version=\"1.0\" ?>\n");
            out.write("<error>ParsingException</error>");
        } catch (URISyntaxException e) {
            response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
            out.write("<?xml version=\"1.0\" ?>\n");
            out.write("<error>The URI provided has a syntax error.</error>");
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
