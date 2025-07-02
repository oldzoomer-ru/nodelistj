package ru.oldzoomer.nodelistj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.oldzoomer.nodelistj.entries.NodelistEntryMap;
import ru.oldzoomer.nodelistj.enums.CurrentNodelistTree;
import ru.oldzoomer.nodelistj.enums.Keywords;

/**
 * Fidonet Nodelist parser
 */
public class NodelistMap {

    private final Map<Integer, NodelistEntryMap> nodelistEntries = new HashMap<>();

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

        try {
            indexNodelist(Files.newInputStream(path));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read file", e);
        }
    }

    /**
     * Nodelist constructor with input stream
     * @param inputStream input stream
     */
    public NodelistMap(InputStream inputStream) {
        indexNodelist(inputStream);
    }

    /**
     * Get all data from nodelist
     * @return {@link Map} of {@link NodelistEntryMap} with data from the nodelist
     * *
     * @see NodelistEntryMap
     */
    public Map<Integer, NodelistEntryMap> getNodelistEntries() {
        return nodelistEntries;
    }

    /**
     * Returns {@link NodelistEntryMap} by address
     *
     * @param address address of the node
     * @return {@link NodelistEntryMap} with data from the nodelist
     */
    public NodelistEntryMap getNodelistEntryMap(String address) {
        if (address == null) {
            throw new IllegalArgumentException("Address is null");
        }

        Matcher matcher = Pattern.compile("^([0-9]+):([0-9]+)/([0-9]+)$").matcher(address);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Incorrect address format");
        }

        int zone = Integer.parseInt(matcher.group(1));
        int network = Integer.parseInt(matcher.group(2));
        int node = Integer.parseInt(matcher.group(3));

        return getNodelistEntryMap(zone, network, node);
    }

    /**
     * Returns {@link NodelistEntryMap} by address
     *
     * @param zone zone number
     * @param network network number
     * @param node node number
     * @return {@link NodelistEntryMap} with data from the nodelist
     */
    public NodelistEntryMap getNodelistEntryMap(int zone, int network, int node) {
        if (zone < 1 || network < 1 || node < 0) {
            throw new IllegalArgumentException("Zone, network or node is not valid");
        }
        if (!nodelistEntries.containsKey(zone)) {
            throw new IllegalArgumentException("Zone not found");
        }
        if (!nodelistEntries.get(zone).children().containsKey(network)) {
            throw new IllegalArgumentException("Network not found");
        }
        if (!nodelistEntries.get(zone).children().get(network).children().containsKey(node)) {
            throw new IllegalArgumentException("Node not found");
        }

        return nodelistEntries.get(zone).children().get(network).children().get(node);
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
            throw new IllegalArgumentException("Zone or network is not valid");
        }
        if (!nodelistEntries.containsKey(zone)) {
            throw new IllegalArgumentException("Zone not found");
        }
        if (!nodelistEntries.get(zone).children().containsKey(network)) {
            throw new IllegalArgumentException("Network not found");
        }

        return nodelistEntries.get(zone).children().get(network);
    }

    /**
     * Returns a map of nodes from the specified zone
     *
     * @param zone zone number
     * @return map of nodes from the specified zone
     */
    public NodelistEntryMap getZoneNodelistEntries(int zone) {
        if (zone < 1) {
            throw new IllegalArgumentException("Zone is not valid");
        }
        if (!nodelistEntries.containsKey(zone)) {
            throw new IllegalArgumentException("Zone not found");
        }

        return nodelistEntries.get(zone);
    }

    /**
     * Reads Fidonet nodelist and indexes it in memory
     *
     * @param streamReader {@link InputStream} of the nodelist
     */
    private void indexNodelist(InputStream streamReader) {
        try (InputStreamReader reader = new InputStreamReader(streamReader);
                BufferedReader bufferedReader = new BufferedReader(reader)) {
            CurrentNodelistTree currentNodelistTree = null;
            int currentZone = 0;
            int currentNetwork = 0;

            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();

                if (!line.startsWith(";") || !line.isBlank()) {
                    // Fixes an issue with the empty keyword at the beginning of the nodelist
                    if (line.startsWith(",")) line = "###" + line;

                    String[] splitLine = line.split(",");

                    if (splitLine.length >= 7) {
                        if (Keywords.fromString(splitLine[0]) == Keywords.ZONE) {
                            nodelistEntries.put(Integer.valueOf(splitLine[1]), generateNodelistEntryMap(splitLine));
                            currentNodelistTree = CurrentNodelistTree.ZONE;
                            currentZone = Integer.parseInt(splitLine[1]);
                        } else if (Keywords.fromString(splitLine[0]) == Keywords.HOST ||
                                Keywords.fromString(splitLine[0]) == Keywords.REGION) {
                            nodelistEntries.get(currentZone).children()
                                    .put(Integer.valueOf(splitLine[1]), generateNodelistEntryMap(splitLine));
                            currentNodelistTree = CurrentNodelistTree.NETWORK;
                            currentNetwork = Integer.parseInt(splitLine[1]);
                        } else {
                            if (currentNodelistTree == CurrentNodelistTree.ZONE) {
                                nodelistEntries.get(currentZone).children()
                                        .put(Integer.valueOf(splitLine[1]), generateNodelistEntryMap(splitLine));
                            } else if (currentNodelistTree == CurrentNodelistTree.NETWORK) {
                                nodelistEntries.get(currentZone).children().get(currentNetwork).children()
                                        .put(Integer.valueOf(splitLine[1]), generateNodelistEntryMap(splitLine));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read nodelist", e);
        }
    }

    /**
     * Generates {@link NodelistEntryMap} from a line of the nodelist
     *
     * @param splitLine line of the nodelist
     * @return {@link NodelistEntryMap} with data from the line
     */
    private NodelistEntryMap generateNodelistEntryMap(String[] splitLine) {
        return new NodelistEntryMap(Keywords.fromString(splitLine[0]), splitLine[2], splitLine[3],
                splitLine[4], splitLine[5], Integer.parseInt(splitLine[6]),
                Arrays.copyOfRange(splitLine, 7, splitLine.length), new HashMap<>());
    }

}