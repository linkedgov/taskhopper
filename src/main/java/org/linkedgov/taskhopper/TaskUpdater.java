package org.linkedgov.taskhopper;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.linkedgov.taskhopper.http.ApplicationSettings;
import org.linkedgov.taskhopper.support.RDFToXOM;

/**
 * Collection of static methods for modifying instance documents to fix tasks.
 *
 * @author tom
 */
public class TaskUpdater {

    private static Logger log = Logger.getLogger(TaskUpdater.class.getName());
    private Connection connection;

    /**
     * Creates TaskUpdater and Connection.
     *
     * @param address The hostname of the eXist server (e.g. "localhost")
     * @param port Port of the eXist server (e.g. 8080).
     */
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

    /**
     * Creates TaskUpdater from existing Connection
     *
     * @param conn A <code>Connection</code> to an eXist server.
     */
    public TaskUpdater(Connection conn) {
        this.connection = conn;
    }

    /**
     * Modify document to mark a value from a task as null.
     *
     * @param document
     * @param issueId
     * @return
     * @throws UnsupportedEncodingException
     * @throws ParsingException
     * @throws ValidityException
     * @throws IOException
     */
    protected static Document nullifyTask(Document document, String issueId)
            throws UnsupportedEncodingException, ParsingException,
            ValidityException, IOException, ClassNotFoundException {
        /* Nullifying a task consists of removing the task from the
         * XML document and removing the reference to the task from
         * the main RDF document. */
        Element root = document.getRootElement();
        Element taskElem = TaskUpdater.getTaskElementFromDocument(document, issueId);
        taskElem.getParent().detach();

        /* Now modify the RDF graph to remove the statement of the form
         * <subject> <potentiallyIncorrect> <issueId> . */
        /* First, we read in the RDF */
        Model model = TaskUpdater.getMainGraphFromDocument(document);

        /* Remove potentiallyIncorrect statements from graph. */
        model = TaskUpdater.removePotentiallyIncorrect(model, issueId);

        /* Merge the RDF graph back into document in place of the original main
         * element. */
        Document rdfOut = RDFToXOM.convertToXOM(model);
        root.getFirstChildElement("main").removeChildren();
        root.getFirstChildElement("main").insertChild(rdfOut.getRootElement().copy(), 0);

        /* Dump the whole document out. */
        return document;
    }

    /**
     * Modify document to mark value as okay: effectively merging main and issue
     * graph.
     *
     * @param document
     * @param issueId
     * @return a modified document with the task graph removed and the main graph updated.
     * @throws UnsupportedEncodingException
     * @throws ParsingException
     * @throws ValidityException
     * @throws IOException
     */
     // TODO: try/catch/finally for the IOException (and elsewhere in class)
    protected static Document markAsOkay(Document document, String issueId)
            throws UnsupportedEncodingException, ParsingException,
            ValidityException, IOException {

        Element root = document.getRootElement();
        Model model = TaskUpdater.getMainGraphFromDocument(document);
        Model taskGraph = TaskUpdater.getTaskGraphFromDocument(document, issueId);
        
        /* Iterate through all statements in the task graph, copy them into
         * main graph. */
        StmtIterator stmts = taskGraph.listStatements();
        while (stmts.hasNext()) {
            Statement stmt = (Statement) stmts.next();
            Statement newStmt = model.createStatement(
                    stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
            model.add(newStmt);
        }

        /* We are done with the TaskGraph, so let's close it. */
        taskGraph.close();
        /* Remove the references from the document graph. */
        model = TaskUpdater.removePotentiallyIncorrect(model, issueId);

        /* Merge the RDF graph back into document in place of the original main
         * element. */
        Document rdfOut = RDFToXOM.convertToXOM(model);
        model.close();
        root.getFirstChildElement("main").removeChildren();
        root.getFirstChildElement("main").insertChild(rdfOut.getRootElement().copy(), 0);

        /* Finally, remove the issue element from the document. */
        TaskUpdater.getTaskElementFromDocument(document, issueId).getParent().detach();

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
    protected static Document editValue(Document document, String taskId,
            String replacementValue, String replacementXsdType)
            throws UnsupportedEncodingException, ParsingException,
            ValidityException, IOException {
        /* In the code and comments below, I refer to two graphs:
         * 
         * 1. the document graph is what is contained in <main />
         * 
         * 2. the issue graph is the named graph in a particular
         *    <issue />.
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

            Statement newStmt = model.createStatement(stmt.getSubject(), stmt.getPredicate(), replacementValueWrapped);

            /* Add to the document graph rather than the task graph. */
            model.add(newStmt);
        }
        /* We are done with the TaskGraph, so let's close it. */
        taskGraph.close();
        /* Remove the references from the document graph. */
        model = TaskUpdater.removePotentiallyIncorrect(model, taskId);

        /* Merge the RDF graph back into document in place of the original main
         * element. */
        Document rdfOut = RDFToXOM.convertToXOM(model);
        model.close();
        root.getFirstChildElement("main").removeChildren();
        root.getFirstChildElement("main").insertChild(rdfOut.getRootElement().copy(), 0);

        /* Finally, detach the task element from the document. */
        TaskUpdater.getTaskElementFromDocument(document, taskId).getParent().detach();

        return document;
    }

    /**
     * Updates document to refer task to an expert.
     *
     * @param document XML representation of the task
     * @param taskID URL of the task
     * @return XML representation of the task with the issue marked as needing an expert.
     */
    protected static Document referToExpert(Document document, String taskId) {
        Element root = document.getRootElement();
        Element taskRDF = TaskUpdater.getTaskElementFromDocument(document, taskId);
        Element task = (Element) taskRDF.getParent();
        Attribute taskType = task.getAttribute("task-type");
        taskType.setLocalName("original-task-type");
        task.addAttribute(new Attribute("task-type",
                ApplicationSettings.needsAnExpert));
        return document;
    }

    /**
     * Removes a potentially-incorrect attribute from the main model.
     *
     * @param model
     * @param taskId
     * @return
     */
    protected static Model removePotentiallyIncorrect(Model model, String taskId) {
        /* Now select "potentiallyIncorrect" property. */
        Property incorrect = model.createProperty(ApplicationSettings.potentiallyIncorrectUri);
        Resource taskIdResource = model.createResource(taskId);
        model.removeAll((Resource) null, incorrect, taskIdResource);
        return model;
    }

    /**
     * Takes a linkedgov document and returns the main graph from it as a Jena
     * model.
     *
     * @param document
     * @throws UnsupportedEncodingException
     * @return RDF model.
     */
    protected static Model getMainGraphFromDocument(Document document)
            throws UnsupportedEncodingException {
        Element root = document.getRootElement();
        Element mainDoc = root.getFirstChildElement("main");
        Element mainDocRDF = mainDoc.getFirstChildElement("RDF",
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        return RDFToXOM.convertFromXOM(mainDocRDF);
    }

    /**
     * Retrieves the task as a Jena graph for a given task ID.
     *
     * @param document
     * @param taskId
     * @return
     * @throws UnsupportedEncodingException
     */
    protected static Model getTaskGraphFromDocument(Document document, String taskId) throws UnsupportedEncodingException {
        Element taskRDF = TaskUpdater.getTaskElementFromDocument(document, taskId);
        if (taskRDF != null) {
            return RDFToXOM.convertFromXOM(taskRDF);
        } else {
            return null;
        }
    }

    /**
     * Retrieves the task element from the document for a given task ID.
     *
     * @param document
     * @param taskId
     * @return
     */
    protected static Element getTaskElementFromDocument(Document document, String taskId) {
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
