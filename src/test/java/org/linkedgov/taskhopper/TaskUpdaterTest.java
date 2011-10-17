package org.linkedgov.taskhopper;

import com.hp.hpl.jena.rdf.model.Model;
import junit.framework.TestCase;
import junitx.util.PrivateAccessor;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

/**
 * Tests for the TaskUpdater static functions: these modify instance documents
 * based on task input completion (the tests don't write to the database though).
 *
 * @author tom
 */
public class TaskUpdaterTest extends TestCase {
    public String doc;

    public TaskUpdaterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();


        this.doc = "<document>" +
                "<dataset href=\"http://linkedgov.org/data/dwp-electricity-use/\" " +
                "title=\"Electricity use by the DWP\" id=\"dwp-electricity-use\" />" +
                "<main>" + "<rdf:RDF xmlns:lg=\"http://linkedgov.org/schema/\"" +
                " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "<rdf:Description rdf:about=\"http://linkedgov.org/data/dwp-electricity-use/1\">" +
                "<lg:date rdf:datatype=\"http://www.w3.org/2001/XMLSchema#date\">2011-09-07</lg:date>" +
                "<lg:potentiallyIncorrect rdf:resource=\"http://linkedgov.org/data/dwp-electricity-use/1/issue/1\"/>" +
                "</rdf:Description>" + "</rdf:RDF>" + "</main>" +
                "<issue task-type=\"http://linkedgov.org/schema/task-types/float-error\" uri=\"http://linkedgov.org/data/dwp-electricity-use/1/issue/1\">" + "<rdf:RDF xmlns:lg=\"http://linkedgov.org/schema/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" + "<rdf:Description rdf:about=\"http://linkedgov.org/data/dwp-electricity-use/1\">" + "<kwhUsed xmlns=\"http://linkedgov.org/schema/\" rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">99,50</kwhUsed>" + "</rdf:Description>" + "</rdf:RDF></issue>" +
                "<issue task-type=\"http://linkedgov.org/schema/task-types/float-error\" uri=\"http://linkedgov.org/data/dwp-electricity-use/1/issue/2\">" + "<rdf:RDF xmlns:lg=\"http://linkedgov.org/schema/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" + "<rdf:Description rdf:about=\"http://linkedgov.org/data/dwp-electricity-use/1\">" + "<kwhUsed xmlns=\"http://linkedgov.org/schema/\" rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">99-50</kwhUsed>" + "</rdf:Description>" + "</rdf:RDF></issue>" +
                "</document>";
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * Test of editValue method, of class TaskUpdater.
     */
    public void testEditValue() throws Exception, Throwable {
        System.out.println("editValue");
        Builder builder = new Builder();
        Document document = builder.build(this.doc, "");
        String issueId = "http://linkedgov.org/data/dwp-electricity-use/1/issue/1";
        Document out = (Document) PrivateAccessor.invoke(TaskUpdater.class,
                "editValue",
                new Class[] {Document.class, String.class, String.class, String.class},
                new Object[] {(Document) document.copy(), issueId, "99.50", null});
        
        Model main = (Model) PrivateAccessor.invoke(TaskUpdater.class,
                "getMainGraphFromDocument",
                new Class[] {Document.class},
                new Object[] {out});
        assertEquals(2, main.size());
    }

    /**
     * Test of getMainElementFromDocument method, and nullifyTask for single issue.
     */
    public void testGetMainElementFromDocument() throws Exception, Throwable {
        System.out.println("getMainGraphFromDocument");

        // setup document.
        Builder builder = new Builder();
        Document document = builder.build(this.doc, "");
        Elements elems = document.getRootElement().getChildElements("issue");
        // test that the XML library works, that we have issues in the document.
        assertEquals(true, elems.get(0) != null);

        // test that our XPath works.
        String issueId = "http://linkedgov.org/data/dwp-electricity-use/1/issue/1";
        String query = String.format("//issue[@uri = '%s']", issueId);
        Nodes nodes = document.query(query);
        assertEquals((Element) nodes.get(0), elems.get(0));

        // test that we can retrieve a Jena graph from the document and that it has
        // two statements: the date and the reference to the potentially incorrect data.
        Model main = (Model) PrivateAccessor.invoke(TaskUpdater.class,
                "getMainGraphFromDocument",
                new Class[] {Document.class},
                new Object[] {document});
        assertEquals(2, main.size());

        // run the document through nullify process, which should remove the reference
        // to the potentially incorrect data.
        Document out = (Document) PrivateAccessor.invoke(TaskUpdater.class,
                "nullifyTask",
                new Class[] {Document.class, String.class},
                new Object[] {document.copy(), issueId});

        // checks to see if the main graph is only one statement now
        Model mainGraphAfterUpdate = (Model) PrivateAccessor.invoke(TaskUpdater.class,
                "getMainGraphFromDocument",
                new Class[] {Document.class},
                new Object[] {out});
        assertEquals(1, mainGraphAfterUpdate.size());
    }


    /**
     * Test nullifyTask with multiple issues.
     * 
     * This test is to ensure that after you run a nullify against a graph with
     * two statements (one being the issue, another being data without issue),
     * that the returned document should contain only one issue and the remaining
     * graph contains only one statement.
     */
    public void testMultipleIssuesInDocumentNullify() throws Exception, Throwable {
        Builder builder = new Builder();
        Document document = builder.build(this.doc, "");
        String issueId = "http://linkedgov.org/data/dwp-electricity-use/1/issue/1";
        assertEquals(2, document.query("//issue").size());
        Document out = (Document) PrivateAccessor.invoke(TaskUpdater.class,
                "nullifyTask",
                new Class[] {Document.class, String.class},
                new Object[] {document.copy(), issueId});

        assertEquals(1, out.query("//issue").size());
        Model mainGraph = (Model) PrivateAccessor.invoke(TaskUpdater.class,
                "getMainGraphFromDocument",
                new Class[] {Document.class},
                new Object[] {out});
        assertEquals(1, mainGraph.size());
    }

    /**
     * Test of getIssueGraphFromDocument method, of class TaskUpdater.
     */
    public void testGetIssueGraphFromDocument() throws Exception, Throwable {
          System.out.println("testGetIssueGraphFromDocument");
          Builder builder = new Builder();
          Document document = builder.build(this.doc, "");
          String issueId = "http://linkedgov.org/data/dwp-electricity-use/1/issue/1";
          Model result = (Model) PrivateAccessor.invoke(TaskUpdater.class,
                "getIssueGraphFromDocument",
                new Class[] {Document.class, String.class},
                new Object[] {document, issueId});
          assertEquals(1, result.size());
    }
}
