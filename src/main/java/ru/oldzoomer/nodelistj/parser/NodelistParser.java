package ru.oldzoomer.nodelistj.parser;

import ru.oldzoomer.nodelistj.entries.NodelistEntry;
import ru.oldzoomer.nodelistj.enums.Keywords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Optimized Fidonet nodelist parser with improved performance and cleaner code structure
 */
public class NodelistParser {
    
    private static final int MIN_FIELDS_REQUIRED = 7;
    private static final String COMMENT_PREFIX = ";";
    private static final String EMPTY_KEYWORD_FIX = "###";
    private static final String FIELD_SEPARATOR = ",";

    private NodelistParser() {
    }

    /**
     * Parse nodelist from input stream with optimized algorithm
     * 
     * @param inputStream input stream containing nodelist data
     * @return list of parsed nodelist entries
     * @throws IOException if reading fails
     */
    public static List<NodelistEntry> parseNodelist(InputStream inputStream) throws IOException {
        List<NodelistEntry> entries = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            ParsingContext context = new ParsingContext();
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (shouldSkipLine(line)) {
                    continue;
                }
                
                NodelistEntry entry = parseLine(line, context);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
        
        return entries;
    }
    
    /**
     * Check if line should be skipped during parsing
     */
    private static boolean shouldSkipLine(String line) {
        return line.startsWith(COMMENT_PREFIX) || line.isBlank();
    }
    
    /**
     * Parse a single line into a NodelistEntry
     */
    private static NodelistEntry parseLine(String line, ParsingContext context) {
        String processedLine = preprocessLine(line);
        String[] fields = processedLine.split(FIELD_SEPARATOR, -1);
        
        if (fields.length < MIN_FIELDS_REQUIRED) {
            return null;
        }
        
        Keywords keyword = Keywords.fromString(fields[0]);
        
        return createNodelistEntry(fields, keyword, context);
    }
    
    /**
     * Preprocess line to handle edge cases
     */
    private static String preprocessLine(String line) {
        // Fix empty keyword issue
        if (line.startsWith(FIELD_SEPARATOR)) {
            return EMPTY_KEYWORD_FIX + line;
        }
        return line;
    }
    
    /**
     * Create NodelistEntry from parsed fields
     */
    private static NodelistEntry createNodelistEntry(String[] fields, Keywords keyword, ParsingContext context) {
        try {
            Integer nodeNumber = parseInteger(fields[1]);
            if (nodeNumber == null) {
                return null;
            }
            
            updateContext(context, keyword, nodeNumber);
            
            Integer zone = context.getCurrentZone();
            Integer network = context.getCurrentNetwork();
            Integer node = determineNodeNumber(keyword, nodeNumber);
            
            if (zone == null || network == null) {
                return null;
            }
            
            Integer baudRate = parseInteger(fields[6]);
            if (baudRate == null) {
                return null;
            }
            
            String[] flags = extractFlags(fields);
            
            return new NodelistEntry(
                zone, network, node, keyword,
                fields[2], fields[3], fields[4], fields[5],
                baudRate, flags
            );
            
        } catch (Exception e) {
            // Skip malformed entries
            return null;
        }
    }
    
    /**
     * Update parsing context based on current entry
     */
    private static void updateContext(ParsingContext context, Keywords keyword, Integer nodeNumber) {
        if (keyword == Keywords.ZONE) {
            context.setCurrentZone(nodeNumber);
            context.setCurrentNetwork(nodeNumber);
            context.setCurrentTree(ParsingContext.TreeLevel.ZONE);
        } else if (keyword == Keywords.HOST || keyword == Keywords.REGION) {
            context.setCurrentNetwork(nodeNumber);
            context.setCurrentTree(ParsingContext.TreeLevel.NETWORK);
        }
        // For regular nodes (null keyword), don't change context
    }
    
    /**
     * Determine the node number based on keyword and context
     */
    private static Integer determineNodeNumber(Keywords keyword, Integer nodeNumber) {
        if (keyword == Keywords.ZONE || keyword == Keywords.HOST || keyword == Keywords.REGION) {
            return 0;
        }
        // For regular nodes and other keywords, use the actual node number
        return nodeNumber;
    }
    
    /**
     * Extract flags from fields array
     */
    private static String[] extractFlags(String[] fields) {
        if (fields.length <= MIN_FIELDS_REQUIRED) {
            return new String[0];
        }
        return Arrays.copyOfRange(fields, MIN_FIELDS_REQUIRED, fields.length);
    }
    
    /**
     * Parse integer with null safety
     */
    private static Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Internal class to track parsing context
     */
    private static class ParsingContext {
        enum TreeLevel { ZONE, NETWORK }
        
        private Integer currentZone;
        private Integer currentNetwork;
        private TreeLevel currentTree;
        
        public Integer getCurrentZone() { return currentZone; }
        public void setCurrentZone(Integer zone) { this.currentZone = zone; }
        
        public Integer getCurrentNetwork() { return currentNetwork; }
        public void setCurrentNetwork(Integer network) { this.currentNetwork = network; }
        
        public TreeLevel getCurrentTree() { return currentTree; }
        public void setCurrentTree(TreeLevel tree) { this.currentTree = tree; }
    }
}