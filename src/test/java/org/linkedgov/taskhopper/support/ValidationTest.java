package org.linkedgov.taskhopper.support;

import junit.framework.TestCase;

/**
 *
 * @author tom
 */
public class ValidationTest extends TestCase {
    
    public ValidationTest(String testName) {
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
     * Test of checkSanityOfJSONPCallback method, of class Validation.
     */
    public void testSanitiseJSONPCallback() {
        System.out.println("sanitiseJSONPCallback");
        assertTrue("hello", Validation.checkSanityOfJSONPCallback("hello"));
        assertFalse("function", Validation.checkSanityOfJSONPCallback("function"));
        assertFalse("alert()", Validation.checkSanityOfJSONPCallback("alert()"));
        assertFalse("  evil", Validation.checkSanityOfJSONPCallback("  evil"));
        assertFalse("<script>", Validation.checkSanityOfJSONPCallback("<script>"));
    }
}
