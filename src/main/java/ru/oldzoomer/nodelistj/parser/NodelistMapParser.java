package ru.oldzoomer.nodelistj.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ru.oldzoomer.nodelistj.entries.NodelistEntryMap;
import ru.oldzoomer.nodelistj.enums.Keywords;

/**
 * Optimized Fidonet nodelist map parser with improved performance
 */
public class NodelistMapParser {
    
    private static final int MIN_FIELDS_REQUIRED = 7;
    private static final String COMMENT_PREFIX = ";";
    private static final String EMPTY_KEYWORD_FIX = "###";
    private static final String FIELD_SEPARATOR = ",";
    
    /**
     * Parse nodelist into hierarchical map structure
     * 
     * @param inputStream input stream containing nodelist data
     * @return map of zones containing networks and nodes
     * @throws IOException if reading fails
     */
    public static Map<Integer, NodelistEntryMap> parseNodelistMap(InputStream inputStream) throws IOException {
        Map<Integer, NodelistEntryMap> nodelistEntries = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            MapParsingContext context = new MapParsingContext();
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (shouldSkipLine(line)) {
                    continue;
                }
                
                processLineForMap(line, nodelistEntries, context);
            }
        }
        
        return nodelistEntries;
    }
    
    /**
     * Check if line should be skipped during parsing
     */
    private static boolean shouldSkipLine(String line) {
        return line.startsWith(COMMENT_PREFIX) || line.isBlank();
    }
    
    /**
     * Process a single line for map structure
     */
    private static void processLineForMap(String line, Map<Integer, NodelistEntryMap> nodelistEntries, 
                                         MapParsingContext context) {
        String processedLine = preprocessLine(line);
        String[] fields = processedLine.split(FIELD_SEPARATOR, -1);
        
        if (fields.length < MIN_FIELDS_REQUIRED) {
            return;
        }
        
        Keywords keyword = Keywords.fromString(fields[0]);
        
        try {
            Integer nodeNumber = parseInteger(fields[1]);
            if (nodeNumber == null) {
                return;
            }
            
            NodelistEntryMap entry = createNodelistEntryMap(fields, keyword);
            insertIntoMap(nodelistEntries, entry, keyword, nodeNumber, context);
            
        } catch (Exception e) {
            // Skip malformed entries
        }
    }
    
    /**
     * Insert entry into the hierarchical map structure
     */
    private static void insertIntoMap(Map<Integer, NodelistEntryMap> nodelistEntries, 
                                     NodelistEntryMap entry, Keywords keyword, 
                                     Integer nodeNumber, MapParsingContext context) {
        if (keyword == Keywords.ZONE) {
            nodelistEntries.put(nodeNumber, entry);
            context.setCurrentZone(nodeNumber);
            context.setCurrentTree(MapParsingContext.TreeLevel.ZONE);
        } else if (keyword == Keywords.HOST || keyword == Keywords.REGION) {
            if (context.getCurrentZone() != null && nodelistEntries.containsKey(context.getCurrentZone())) {
                nodelistEntries.get(context.getCurrentZone()).children().put(nodeNumber, entry);
                context.setCurrentNetwork(nodeNumber);
                context.setCurrentTree(MapParsingContext.TreeLevel.NETWORK);
            }
        } else {
            // Regular node entry (keyword is null or other types)
            if (context.getCurrentZone() != null && nodelistEntries.containsKey(context.getCurrentZone())) {
                if (context.getCurrentTree() == MapParsingContext.TreeLevel.ZONE) {
                    // Direct child of zone
                    nodelistEntries.get(context.getCurrentZone()).children().put(nodeNumber, entry);
                } else if (context.getCurrentTree() == MapParsingContext.TreeLevel.NETWORK && 
                          context.getCurrentNetwork() != null) {
                    // Child of network
                    NodelistEntryMap networkEntry = nodelistEntries.get(context.getCurrentZone())
                                                                  .children()
                                                                  .get(context.getCurrentNetwork());
                    if (networkEntry != null) {
                        networkEntry.children().put(nodeNumber, entry);
                    }
                }
            }
        }
    }
    
    /**
     * Preprocess line to handle edge cases
     */
    private static String preprocessLine(String line) {
        if (line.startsWith(FIELD_SEPARATOR)) {
            return EMPTY_KEYWORD_FIX + line;
        }
        return line;
    }
    
    /**
     * Create NodelistEntryMap from parsed fields
     */
    private static NodelistEntryMap createNodelistEntryMap(String[] fields, Keywords keyword) {
        Integer baudRate = parseInteger(fields[6]);
        if (baudRate == null) {
            baudRate = 0; // Default value for malformed baud rate
        }
        
        String[] flags = extractFlags(fields);
        
        return new NodelistEntryMap(
            keyword, fields[2], fields[3], fields[4], fields[5],
            baudRate, flags, new HashMap<>()
        );
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
     * Internal class to track map parsing context
     */
    private static class MapParsingContext {
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