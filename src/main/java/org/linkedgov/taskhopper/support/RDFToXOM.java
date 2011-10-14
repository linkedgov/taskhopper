package org.linkedgov.taskhopper.support;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

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

    /**
     * Converts XOM document containing RDF/XML into Jena model.
     *
     * @param doc
     * @return Jena model of the RDF/XML document.
     */
    public static Model convertFromXOM(Document doc) {
        Model model = ModelFactory.createDefaultModel();
        InputStream mainDocStream = null;
        try {
            mainDocStream = new ByteArrayInputStream(doc.toXML().getBytes("UTF-8"));
            model.read(mainDocStream, "");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(RDFToXOM.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                mainDocStream.close();
            } catch (IOException ex) {
                Logger.getLogger(RDFToXOM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return model;
    }

    /**
     * Converts a XOM element into a Jena model.
     *
     * @param elem
     * @return
     */
    public static Model convertFromXOM(Element elem) {
        Element e = (Element) elem.copy();
        Document doc = new Document(e);
        return RDFToXOM.convertFromXOM(doc);
    }
}
