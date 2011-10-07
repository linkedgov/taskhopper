package org.linkedgov.taskhopper.support;

import com.hp.hpl.jena.rdf.model.Model;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import nu.xom.Builder;
import nu.xom.Document;

/**
 * Convenience class for turning Jena models into RDF/XML Documents with XOM.
 *
 * @author tom
 */
public class RDFToXOM {
    /**
     * Converts Jena model into RDF/XML document.
     *
     * @param model
     * @return RDF/XML document as XOM <code>Document</code>.
     */
    public static Document convertToXOM(Model model) {
        try {
            ByteArrayOutputStream rdfOutStream = new ByteArrayOutputStream();
            model.write(rdfOutStream, "RDF/XML-ABBREV");
            model.close();
            Builder builder = new Builder();
            Document rdfOut = builder.build(new ByteArrayInputStream(rdfOutStream.toByteArray()));
            return rdfOut;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
