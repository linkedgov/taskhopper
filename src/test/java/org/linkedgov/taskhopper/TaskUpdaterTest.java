package org.linkedgov.taskhopper;

import com.hp.hpl.jena.rdf.model.Model;
import junit.framework.TestCase;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

public class TaskUpdaterTest extends TestCase {

    public TaskUpdaterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of update method, of class TaskUpdater.
     */
//    public void testUpdate() {
//        System.out.println("update");
//        Task task = null;
//        Map<String, String> values = null;
//        TaskUpdater instance = null;
//        instance.update(task, values);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of updateById method, of class TaskUpdater.
     */
//    public void testUpdateById() {
//        System.out.println("updateById");
//        String id = "";
//        TaskUpdater instance = null;
//        instance.updateById(id);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of merge method, of class TaskUpdater.
     */
//    public void testMerge() {
//        System.out.println("merge");
//        TaskUpdater instance = null;
//        instance.merge();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of nullifyTask method, of class TaskUpdater.
     */
//    public void testNullifyTask() throws Exception {
//        System.out.println("nullifyTask");
//        Document document = null;
//        String taskId = "";
//        Document expResult = null;
//        Document result = TaskUpdater.nullifyTask(document, taskId);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of editValue method, of class TaskUpdater.
     */
//    public void testEditValue() throws Exception {
//        System.out.println("editValue");
//        Document document = null;
//        String taskId = "";
//        String replacementValue = "";
//        String replacementXsdType = "";
//        Document expResult = null;
//        Document result = TaskUpdater.editValue(document, taskId, replacementValue, replacementXsdType);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of getMainElementFromDocument method, of class TaskUpdater.
     */
    public void testGetMainElementFromDocument() throws Exception {
        System.out.println("getMainGraphFromDocument");
        String doc = "<document>" +
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
        Builder builder = new Builder();
        Document document = builder.build(doc, "");
        Elements elems = document.getRootElement().getChildElements("issue");
        assertEquals(true, elems.get(0) != null);

        String issueId = "http://linkedgov.org/data/dwp-electricity-use/1/issue/1";
        String query = String.format("//issue[@uri = '%s']", issueId);
        Nodes nodes = document.query(query);
        assertEquals((Element) nodes.get(0), elems.get(0));

        Model main = TaskUpdater.getMainGraphFromDocument(document);
        assertEquals(2, main.size());

        Document out = TaskUpdater.nullifyTask((Document) document.copy(), issueId);

        Model issueOne = TaskUpdater.getTaskGraphFromDocument(document, issueId);
        assertEquals(1, issueOne.size());

//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }


    public void testMultipleIssuesInDocumentNullify() throws Exception {
        String doc = "<document>" +
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
        Builder builder = new Builder();
        Document document = builder.build(doc, "");
        String issueId = "http://linkedgov.org/data/dwp-electricity-use/1/issue/1";
        assertEquals(2, document.query("//issue").size());
        Document out = TaskUpdater.nullifyTask((Document) document.copy(), issueId);
        assertEquals(1, out.query("//issue").size());
    }

    /**
     * Test of getTaskGraphFromDocument method, of class TaskUpdater.
     */
//    public void testGetTaskGraphFromDocument() throws Exception {
//        System.out.println("getTaskGraphFromDocument");
//        Document document = null;
//        String taskId = "";
//        Model expResult = null;
//        Model result = TaskUpdater.getTaskGraphFromDocument(document, taskId);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
