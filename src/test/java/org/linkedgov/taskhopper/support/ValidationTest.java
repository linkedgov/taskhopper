package org.linkedgov.taskhopper.support;

import junit.framework.TestCase;

/**
 * Unit tests for the JSONP callback sanity check.
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
    public void testcheckSanityOfJSONPCallback() {
        System.out.println("checkSanityJSONPCallback");
        assertTrue("hello", Validation.checkSanityOfJSONPCallback("hello"));
        assertFalse("alert()", Validation.checkSanityOfJSONPCallback("alert()"));
        assertFalse("a-b", Validation.checkSanityOfJSONPCallback("a-b"));
        assertFalse("23foo", Validation.checkSanityOfJSONPCallback("23foo"));
        assertTrue("foo23", Validation.checkSanityOfJSONPCallback("foo23"));
        assertTrue("$210", Validation.checkSanityOfJSONPCallback("$210"));
        assertTrue("_bar", Validation.checkSanityOfJSONPCallback("_bar"));
        assertTrue("some_var", Validation.checkSanityOfJSONPCallback("some_var"));
        assertTrue("a.b", Validation.checkSanityOfJSONPCallback("a.b"));
        assertFalse("function", Validation.checkSanityOfJSONPCallback("function"));
        assertFalse("  evil", Validation.checkSanityOfJSONPCallback("  evil"));
        assertFalse("<script>", Validation.checkSanityOfJSONPCallback("<script>"));
        assertFalse("$.23", Validation.checkSanityOfJSONPCallback("$.23"));

        // Unicode (non-ASCII) characters should return false as we are disallowing them
        assertFalse("Stra\u00dfe", Validation.checkSanityOfJSONPCallback("Stra\u00dfe"));
        assertFalse("\u0020", Validation.checkSanityOfJSONPCallback("\u0020"));

        // This is a Unicode representation of an ASCII character, so should be fine.
        assertTrue("\u0062", Validation.checkSanityOfJSONPCallback("\u0062"));
    }
}
