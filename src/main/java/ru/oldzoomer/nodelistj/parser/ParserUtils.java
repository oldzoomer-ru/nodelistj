package ru.oldzoomer.nodelistj.parser;

import java.util.Arrays;

/**
 * Shared parsing utilities for Fidonet Nodelist parsers.
 * Consolidates common constants, helper methods, and parsing context.
 */
final class ParserUtils {

    static final int MIN_FIELDS_REQUIRED = 7;
    static final String COMMENT_PREFIX = ";";
    static final String EMPTY_KEYWORD_FIX = "###";
    static final String FIELD_SEPARATOR = ",";

    private ParserUtils() {
    }

    /**
     * Checks if the line is a comment or blank and should be skipped.
     */
    static boolean shouldSkipLine(String line) {
        return line.startsWith(COMMENT_PREFIX) || line.isBlank();
    }

    /**
     * Fixes lines that start with a field separator (empty keyword).
     */
    static String preprocessLine(String line) {
        if (line.startsWith(FIELD_SEPARATOR)) {
            return EMPTY_KEYWORD_FIX + line;
        }
        return line;
    }

    /**
     * Safely parses an integer, returning {@code null} on failure.
     */
    static Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Extracts flags from the fields array (everything beyond the 7 required fields).
     */
    static String[] extractFlags(String[] fields) {
        if (fields.length <= MIN_FIELDS_REQUIRED) {
            return new String[0];
        }
        return Arrays.copyOfRange(fields, MIN_FIELDS_REQUIRED, fields.length);
    }

    /**
     * Parsing context shared by both flat and map parsers.
     * Tracks the current zone, network, and tree level as the parser walks the nodelist.
     */
    static final class ParsingContext {
        enum TreeLevel { ZONE, NETWORK }

        private Integer currentZone;
        private Integer currentNetwork;
        private TreeLevel currentTree;

        Integer getCurrentZone() { return currentZone; }
        void setCurrentZone(Integer zone) { this.currentZone = zone; }

        Integer getCurrentNetwork() { return currentNetwork; }
        void setCurrentNetwork(Integer network) { this.currentNetwork = network; }

        TreeLevel getCurrentTree() { return currentTree; }
        void setCurrentTree(TreeLevel tree) { this.currentTree = tree; }
    }
}
