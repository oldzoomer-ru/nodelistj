package ru.gavrilovegor519;

import ru.gavrilovegor519.dto.NodelistEntryDto;
import ru.gavrilovegor519.enums.CurrentNodelistTree;
import ru.gavrilovegor519.enums.Keywords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fidonet Nodelist parser
 */
public class Nodelist {

    private final List<NodelistEntryDto> nodelistEntries = new ArrayList<>();

    /**
     * Nodelist constructor with path to nodelist
     * @param path path to nodelist
     */
    public Nodelist(Path path) {
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
    public Nodelist(InputStream inputStream) {
        indexNodelist(inputStream);
    }

    /**
     * Get all data from nodelist
     * @return {@link List} of {@link NodelistEntryDto} with data from the nodelist
     * *
     * @see NodelistEntryDto
     */
    public List<NodelistEntryDto> getNodelistEntries() {
        return nodelistEntries;
    }

    /**
     * Returns {@link NodelistEntryDto} by address
     *
     * @param address address of the node
     * @return {@link NodelistEntryDto} with data from the nodelist
     */
    public NodelistEntryDto getNodelistEntry(String address) {
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

        return getNodelistEntry(zone, network, node);
    }

    public NodelistEntryDto getNodelistEntry(int zone, int network, int node) {
        if (zone < 1 || network < 1 || node < 0) {
            throw new IllegalArgumentException("Zone, network or node is not valid");
        }

        return nodelistEntries.stream()
                .filter(nodelistEntryDto -> nodelistEntryDto.number() == zone)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Zone not found"))
                .children().stream()
                .filter(nodelistEntryDto -> nodelistEntryDto.number() == network)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Network not found"))
                .children().stream()
                .filter(nodelistEntryDto -> nodelistEntryDto.number() == node)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Node not found"));
    }

    /**
     * Get a list of nodes from the specified network
     *
     * @param zone zone number
     * @param network network number
     * @return list of nodes from the specified network
     */
    public NodelistEntryDto getNetworkNodelistEntries(int zone, int network) {
        return nodelistEntries.stream()
                .filter(nodelistEntryDto -> nodelistEntryDto.number() == zone)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Zone not found"))
                .children().stream()
                .filter(nodelistEntryDto -> nodelistEntryDto.number() == network)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Network not found"));
    }

    /**
     * Returns a list of nodes from the specified zone
     *
     * @param zone zone number
     * @return list of nodes from the specified zone
     */
    public NodelistEntryDto getZoneNodelistEntries(int zone) {
        return nodelistEntries.stream()
                .filter(nodelistEntryDto -> nodelistEntryDto.number() == zone)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Zone not found"));
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

            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();

                if (!line.startsWith(";") || !line.isBlank()) {
                    if (line.startsWith(",")) line = "###" + line; // Fixes an issue with the empty keyword at the beginning of the nodelist

                    String[] splitLine = line.split(",");

                    if (splitLine.length >= 7) {
                        if (Keywords.fromString(splitLine[0]) == Keywords.ZONE) {
                            nodelistEntries.add(generateNodelistEntry(splitLine));
                            currentNodelistTree = CurrentNodelistTree.ZONE;
                        } else if (Keywords.fromString(splitLine[0]) == Keywords.HOST || Keywords.fromString(splitLine[0]) == Keywords.REGION) {
                            nodelistEntries.getLast().children().add(generateNodelistEntry(splitLine));
                            currentNodelistTree = CurrentNodelistTree.NETWORK;
                        } else {
                            if (currentNodelistTree == CurrentNodelistTree.ZONE) {
                                nodelistEntries.getLast().children().add(generateNodelistEntry(splitLine));
                            } else if (currentNodelistTree == CurrentNodelistTree.NETWORK) {
                                nodelistEntries.getLast().children().getLast().children().add(generateNodelistEntry(splitLine));
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
     * Generates {@link NodelistEntryDto} from a line of the nodelist
     *
     * @param splitLine line of the nodelist
     * @return {@link NodelistEntryDto} with data from the line
     */
    private NodelistEntryDto generateNodelistEntry(String[] splitLine) {
        return new NodelistEntryDto(Keywords.fromString(splitLine[0]),
                Integer.parseInt(splitLine[1]), splitLine[2], splitLine[3],
                splitLine[4], splitLine[5], Integer.parseInt(splitLine[6]),
                Arrays.copyOfRange(splitLine, 7, splitLine.length), new ArrayList<>());
    }

}
