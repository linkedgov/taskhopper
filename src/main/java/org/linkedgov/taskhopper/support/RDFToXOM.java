package org.linkedgov.taskhopper.support;

import com.hp.hpl.jena.rdf.model.Model;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import nu.xom.Builder;
import nu.xom.Document;

public class RDFToXOM {
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
