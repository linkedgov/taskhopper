package org.linkedgov.taskhopper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class TaskUpdater {

    private Connection connection;

    public TaskUpdater(String address, Integer port) {
        if (address == null) {
            throw new NullPointerException("Address must be set.");
        }
        if (port == null) {
            port = 8080;
        }
        Connection newConn = new Connection(address, port);
        this.connection = newConn;
    }

    public TaskUpdater(Connection conn) {
        this.connection = conn;
    }

    /**
     * Method to modify document to mark a value from a task as null.
     *
     * @param document
     * @param taskId
     * @return
     * @throws UnsupportedEncodingException
     * @throws ParsingException
     * @throws ValidityException
     * @throws IOException
     */
    public static Document nullifyTask(Document document, String taskId)
            throws UnsupportedEncodingException, ParsingException,
                ValidityException, IOException {
        /* Nullifying a task consists of removing the task from the
         * XML document and removing the reference to the task from
         * the main RDF document. */
        Element root = document.getRootElement();
        String taskQuery = String.format("//issue[@uri = '%s']", taskId);
        Nodes taskElems = root.query(taskQuery);
        for (int i = 0; i < taskElems.size(); i++) {
            Node task = taskElems.get(i);
            task.detach();
        }

        /* Now modify the RDF graph to remove the statement of the form
         * <subject> <potentiallyIncorrect> <issueId> . */
        /* First, we read in the RDF */
        Element mainDoc = root.getFirstChildElement("main");
        Element mainDocRDF = mainDoc.getFirstChildElement("RDF",
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        InputStream mainDocStream = new ByteArrayInputStream(
                mainDocRDF.toXML().getBytes("UTF-8"));
        Model model = ModelFactory.createDefaultModel();
        model.read(mainDocStream, "");

        /* Now select "potentiallyIncorrect" property. */
        Property incorrect = model.createProperty(TaskSelector.potentiallyIncorrectUri);
        Resource taskIdResource = model.createResource(taskId);
        StmtIterator stmts = model.listStatements((Resource) null,
                incorrect,
                (Resource) taskIdResource);

        /* Remove potentiallyIncorrect statements from graph. */
        while (stmts.hasNext()) {
            Statement stmt = (Statement) stmts.next();
            model.remove(stmt);
        }

        /* I/O jiggerypokery to move data from Jena back to XML document. */
        ByteArrayOutputStream rdfOutStream = new ByteArrayOutputStream();
        model.write(rdfOutStream, "RDF/XML-ABBREV");
        model.close();

        Builder builder = new Builder();
        Document rdfOut = 
                builder.build(new ByteArrayInputStream(rdfOutStream.toByteArray()));

        /* Merge the RDF graph back into document in place of the original main
         * element. */
        mainDoc.removeChildren();
        mainDoc.insertChild(rdfOut.getRootElement().copy(), 0);

        /* Dump the whole document out. */
        return document;
    }
}
