package org.linkedgov.taskhopper.http;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.linkedgov.taskhopper.support.Validation;

/**
 * Filter to throw out JSONP requests with dodgy callback names.
 *
 * Validation takes place with Validation.checkSanityOfJSONPCallback
 *
 * If the validation fails, send back a 400 error with message.
 *
 * @author tom
 */
public class JSONPCallbackNameSecurityFilter implements Filter {

    private FilterConfig filterConfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String callback = request.getParameter("callback");
        if (callback != null && callback.length() > 0) {
            boolean validates = Validation.checkSanityOfJSONPCallback(callback);
            if (validates) {
                chain.doFilter(request, response);
            } else {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.sendError(httpResponse.SC_BAD_REQUEST,
                        "JSON callback name should be a valid JavaScript identifier and use only ASCII characters.");
                chain.doFilter(request, httpResponse);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    public void destroy() {
        this.filterConfig = null;
    }
}
