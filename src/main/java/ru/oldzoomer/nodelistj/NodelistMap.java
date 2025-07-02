package ru.oldzoomer.nodelistj;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.oldzoomer.nodelistj.entries.NodelistEntryMap;
import ru.oldzoomer.nodelistj.parser.NodelistMapParser;

/**
 * Optimized Fidonet Nodelist map parser with improved performance
 */
public class NodelistMap {

    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^([0-9]+):([0-9]+)/([0-9]+)$");
    private final Map<Integer, NodelistEntryMap> nodelistEntries;

    /**
     * Nodelist constructor with path to nodelist
     * @param path path to nodelist
     */
    public NodelistMap(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path is null");
        }

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("File does not exist");
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            nodelistEntries = NodelistMapParser.parseNodelistMap(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read file", e);
        }
    }

    /**
     * Nodelist constructor with input stream
     * @param inputStream input stream
     */
    public NodelistMap(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        
        try {
            nodelistEntries = NodelistMapParser.parseNodelistMap(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse nodelist", e);
        }
    }

    /**
     * Get all data from nodelist
     * @return {@link Map} of {@link NodelistEntryMap} with data from the nodelist
     * 
     * @see NodelistEntryMap
     */
    public Map<Integer, NodelistEntryMap> getNodelistEntries() {
        return nodelistEntries;
    }

    /**
     * Returns {@link NodelistEntryMap} by address string
     *
     * @param address address of the node in format "zone:network/node"
     * @return {@link NodelistEntryMap} with data from the nodelist
     */
    public NodelistEntryMap getNodelistEntryMap(String address) {
        if (address == null) {
            throw new IllegalArgumentException("Address is null");
        }

        Matcher matcher = ADDRESS_PATTERN.matcher(address);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Incorrect address format. Expected format: zone:network/node");
        }

        int zone = Integer.parseInt(matcher.group(1));
        int network = Integer.parseInt(matcher.group(2));
        int node = Integer.parseInt(matcher.group(3));

        return getNodelistEntryMap(zone, network, node);
    }

    /**
     * Returns {@link NodelistEntryMap} by address components
     *
     * @param zone zone number
     * @param network network number
     * @param node node number
     * @return {@link NodelistEntryMap} with data from the nodelist
     */
    public NodelistEntryMap getNodelistEntryMap(int zone, int network, int node) {
        validateAddress(zone, network, node);
        
        NodelistEntryMap zoneEntry = nodelistEntries.get(zone);
        if (zoneEntry == null) {
            throw new IllegalArgumentException("Zone " + zone + " not found");
        }
        
        NodelistEntryMap networkEntry = zoneEntry.children().get(network);
        if (networkEntry == null) {
            throw new IllegalArgumentException("Network " + network + " not found in zone " + zone);
        }
        
        NodelistEntryMap nodeEntry = networkEntry.children().get(node);
        if (nodeEntry == null) {
            throw new IllegalArgumentException("Node " + node + " not found in network " + zone + ":" + network);
        }

        return nodeEntry;
    }

    /**
     * Get a map of nodes from the specified network
     *
     * @param zone zone number
     * @param network network number
     * @return map of nodes from the specified network
     */
    public NodelistEntryMap getNetworkNodelistEntries(int zone, int network) {
        if (zone < 1 || network < 1) {
            throw new IllegalArgumentException("Zone and network must be positive numbers");
        }
        
        NodelistEntryMap zoneEntry = nodelistEntries.get(zone);
        if (zoneEntry == null) {
            throw new IllegalArgumentException("Zone " + zone + " not found");
        }
        
        NodelistEntryMap networkEntry = zoneEntry.children().get(network);
        if (networkEntry == null) {
            throw new IllegalArgumentException("Network " + network + " not found in zone " + zone);
        }

        return networkEntry;
    }

    /**
     * Returns a map of nodes from the specified zone
     *
     * @param zone zone number
     * @return map of nodes from the specified zone
     */
    public NodelistEntryMap getZoneNodelistEntries(int zone) {
        if (zone < 1) {
            throw new IllegalArgumentException("Zone must be a positive number");
        }
        
        NodelistEntryMap zoneEntry = nodelistEntries.get(zone);
        if (zoneEntry == null) {
            throw new IllegalArgumentException("Zone " + zone + " not found");
        }

        return zoneEntry;
    }
    
    /**
     * Validate address components
     */
    private void validateAddress(int zone, int network, int node) {
        if (zone < 1) {
            throw new IllegalArgumentException("Zone must be a positive number");
        }
        if (network < 1) {
            throw new IllegalArgumentException("Network must be a positive number");
        }
        if (node < 0) {
            throw new IllegalArgumentException("Node must be a non-negative number");
        }
    }
}