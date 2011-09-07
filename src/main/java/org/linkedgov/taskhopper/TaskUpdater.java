package org.linkedgov.taskhopper;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
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
import java.util.Map;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class TaskUpdater {

    private Connection connection;

    // TODO: javaDoc this constructor
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

    // TODO: javaDoc this constructor
    public TaskUpdater(Connection conn) {
        this.connection = conn;
    }

    /* TODO: method for modifying one of the issues (by issue URI) and then
     * modifying the graph */
    public void update(Task task, Map<String, String> values) {
        if (values.containsKey("null") && values.get("null").equals("true")) {
            // we need to update the graph to reflect a null value
        }
    }

    public void updateById(String id) {
    }

    public void merge() {
    }
    /* TODO: servlet for serving the above */

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
        // TODO: switch to getTaskElementFromDocument.
        String taskQuery = String.format("//issue[@uri = '%s']", taskId);
        Nodes taskElems = root.query(taskQuery);
        for (int i = 0; i < taskElems.size(); i++) {
            Node task = taskElems.get(i);
            task.detach();
        }

        /* Now modify the RDF graph to remove the statement of the form
         * <subject> <potentiallyIncorrect> <issueId> . */
        /* First, we read in the RDF */
        Model model = TaskUpdater.getMainGraphFromDocument(document);

        /* Remove potentiallyIncorrect statements from graph. */
        model = TaskUpdater.removePotentiallyIncorrect(model, taskId);

        /* I/O jiggerypokery to move data from Jena back to XML document. */
        ByteArrayOutputStream rdfOutStream = new ByteArrayOutputStream();
        model.write(rdfOutStream, "RDF/XML-ABBREV");
        model.close();

        Builder builder = new Builder();
        Document rdfOut =
                builder.build(new ByteArrayInputStream(rdfOutStream.toByteArray()));

        /* Merge the RDF graph back into document in place of the original main
         * element. */
        root.getFirstChildElement("main").removeChildren();
        root.getFirstChildElement("main").insertChild(rdfOut.getRootElement().copy(), 0);

        /* Dump the whole document out. */
        return document;
    }

    /**
     * Resolves a task by editing the value of a literal and then merges that
     * graph into the main graph, the removes the issue from the document and
     * saves the document left at the end into the database.
     *
     * @param document
     * @param taskId the URI of the task
     * @param replacementValue replacement string
     * @param replacementXsdType replacement type (not yet supported, pass null)
     * @return the updated representation of the document.
     * @throws UnsupportedEncodingException
     * @throws ParsingException
     * @throws ValidityException
     * @throws IOException
     */
    public static Document editValue(Document document, String taskId,
            String replacementValue, String replacementXsdType)
            throws UnsupportedEncodingException, ParsingException,
            ValidityException, IOException {
        /* In the code and comments below, I refer to two graphs:
         * 
         * 1. the document graph is what is contained in <main />
         * 
         * 2. the task graph is the named graph in a particular
         *    <task />.
         */
        Element root = document.getRootElement();
        Model model = TaskUpdater.getMainGraphFromDocument(document);



        Model taskGraph = TaskUpdater.getTaskGraphFromDocument(document, taskId);
        StmtIterator stmts = taskGraph.listStatements();
        while (stmts.hasNext()) {
            Statement stmt = (Statement) stmts.next();

            /* Type mangling. We don't want to change the type. We may need to
             * do so in the future to implement replacementXsdType.
             *
             * Until that point, we'll respect the type on the existing literal
             * including if it has no type (in which case existingDatatype
             * will be null).
             */
            RDFDatatype existingDatatype = null;

            if (stmt.getObject().isLiteral()) {
                Literal existingLiteral = (Literal) stmt.getObject();
                existingDatatype = existingLiteral.getDatatype();
            }
            Literal replacementValueWrapped = model.createTypedLiteral(
                    replacementValue, existingDatatype);

            /* changeObject mutates the graph! The word 'change' kind of
             * hints at that, but it is still potentially surprising.
             * Given what we are intending to do with the statement,
             * this is an unnecessary side-effect, but we are going to
             * be getting rid of the contents of the taskGraph anyway.
             */

            Statement newStmt = stmt.changeObject(replacementValueWrapped);
            /* Add to the document graph rather than the task graph. */
            model.add(newStmt);
        }
        /* We are done with the TaskGraph, so let's close it. */
        taskGraph.close();
        /* Remove the references from the document graph. */
        TaskUpdater.removePotentiallyIncorrect(model, taskId);

        /* Now update the XML: write us a new main element. */
        ByteArrayOutputStream rdfOutStream = new ByteArrayOutputStream();
        model.write(rdfOutStream, "RDF/XML-ABBREV");
        model.close();

        Builder builder = new Builder();
        Document rdfOut =
                builder.build(new ByteArrayInputStream(rdfOutStream.toByteArray()));

        /* Merge the RDF graph back into document in place of the original main
         * element. */
        root.getFirstChildElement("main").removeChildren();
        root.getFirstChildElement("main").insertChild(rdfOut.getRootElement().copy(), 0);

        /* Finally, detach the task element from the document. */
        TaskUpdater.getTaskElementFromDocument(document, taskId).getParent().detach();

        return document;
    }

    /**
     * Takes a linkedgov document and returns the main graph from it as a Jena
     * model.
     *
     * @param document
     * @throws UnsupportedEncodingException
     * @return RDF model.
     */
    private static Model getMainGraphFromDocument(Document document)
            throws UnsupportedEncodingException {
        Element root = document.getRootElement();
        Element mainDoc = root.getFirstChildElement("main");
        Element mainDocRDF = mainDoc.getFirstChildElement("RDF",
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        InputStream mainDocStream = new ByteArrayInputStream(
                mainDocRDF.toXML().getBytes("UTF-8"));
        Model model = ModelFactory.createDefaultModel();
        model.read(mainDocStream, "");
        return model;
    }

    /**
     * Removes a potentially-incorrect attribute from the main model.
     *
     * @param model
     * @param taskId
     * @return
     */
    private static Model removePotentiallyIncorrect(Model model, String taskId) {
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
        return model;
    }

    /**
     * Takes a document and returns a 'task graph' as a Jena model.
     *
     * @param document
     * @param taskId
     * @return
     * @throws UnsupportedEncodingException
     */
    private static Model getTaskGraphFromDocument(Document document, String taskId) throws UnsupportedEncodingException {
        Element taskRDF = TaskUpdater.getTaskElementFromDocument(document, taskId);
        if (taskRDF != null) {
            InputStream mainDocStream = new ByteArrayInputStream(
                    taskRDF.toXML().getBytes("UTF-8"));
            Model model = ModelFactory.createDefaultModel();
            model.read(mainDocStream, "");
            return model;
        } else {
            return null;
        }
    }

    // TODO: javaDoc this method
    private static Element getTaskElementFromDocument(Document document, String taskId) {
        Element root = document.getRootElement();
        String taskQuery = String.format("//issue[@uri = '%s']", taskId);
        Nodes taskElems = root.query(taskQuery);
        if (taskElems.size() == 0) {
            return null;
        } else {
            Element task = (Element) taskElems.get(0);
            Element taskRDF = task.getFirstChildElement("RDF",
                    "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            return taskRDF;
        }
    }
}