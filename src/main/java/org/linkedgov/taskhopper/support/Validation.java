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
            if (callback.matches(jsKeyword)) {
                return false;
            }
        }

        if (!callback.matches("^[$_\\p{L}][$_\\p{L}\\p{Mn}\\p{Mc}\\p{Nd}\\p{Pc}\u200C\u200D\\.]*+$")) {
            return false;
        }

        return true;
    }
}
