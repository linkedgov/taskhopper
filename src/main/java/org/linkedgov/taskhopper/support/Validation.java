package org.linkedgov.taskhopper.support;

/**
 * Provides a variety of validations including JSONP callback validation.
 *
 * @author tom
 */
public class Validation {

    /**
     * Checks that a JSONP callback is a valid JavaScript identifier.
     *
     * A valid JSONP callback is a JavaScript identifier:
     * 1. Segments are split using "."
     * 2. The first character of a segment must be a-z, A-Z, $ or _
     * 3. Subsequent characters in a segment must be a-z, A-Z, $, _ or 0-9.
     * 4. The full name of the identifier cannot be a JavaScript keyword.
     *
     * See http://stackoverflow.com/questions/1661197/valid-characters-for-javascript-variable-names
     * and http://tav.espians.com/sanitising-jsonp-callback-identifiers-for-security.html
     * for details on JSONP callback validation.
     *
     * @param callback
     * @return
     */
    public static boolean checkSanityOfJSONPCallback(String callback) {
        String[] jsKeywords = {"abstract", "boolean", "break", "byte", "case",
            "catch", "char", "class", "const", "continue", "debugger",
            "default", "delete", "do", "double", "else", "enum", "export",
            "extends", "false", "final", "finally", "float", "for", "function",
            "goto", "if", "implements", "import", "in", "instanceof",
            "int", "interface", "long", "native", "new", "null", "package",
            "private", "protected", "public", "return", "short", "static",
            "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "true", "try", "typeof", "var", "void", "volatile",
            "while", "with"};

        for (String jsKeyword : jsKeywords) {
            if (callback.equals(jsKeyword)) {
                return false;
            }
        }
        
        String[] segments = callback.split("\\.");
        for (String segment : segments) {
            if (!segment.matches("^[a-zA-Z_$][a-zA-Z0-9_$]*$")) {
                return false;
            }
        }

        return true;
    }
}
