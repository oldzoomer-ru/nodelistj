package ru.oldzoomer.nodelistj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.oldzoomer.nodelistj.entries.NodelistEntry;
import ru.oldzoomer.nodelistj.enums.CurrentNodelistTree;
import ru.oldzoomer.nodelistj.enums.Keywords;

/**
 * Fidonet Nodelist parser
 */
public class Nodelist {

    private final List<NodelistEntry> nodelistRoot;

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
            nodelistRoot = indexNodelist(Files.newInputStream(path));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read file", e);
        }
    }

    public Nodelist(List<NodelistEntry> nodelistRoot) {
        this.nodelistRoot = nodelistRoot;
    }

    /**
     * Nodelist constructor with input stream
     * @param inputStream input stream
     */
    public Nodelist(InputStream inputStream) {
        nodelistRoot = indexNodelist(inputStream);
    }

    /**
     * Get nodelist
     *
     * @return nodelist
     */
    public List<NodelistEntry> getNodelist() {
        return nodelistRoot;
    }

    /**
     * Reads Fidonet nodelist and indexes it in memory
     *
     * @param streamReader {@link InputStream} of the nodelist
     */
    private List<NodelistEntry> indexNodelist(InputStream streamReader) {
        List<NodelistEntry> nodelistRoot = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(streamReader))) {
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
                            nodelistRoot.add(generateNodelistEntry(splitLine, Integer.valueOf(splitLine[1]),
                                    Integer.valueOf(splitLine[1]), null));
                            currentNodelistTree = CurrentNodelistTree.ZONE;
                            currentZone = Integer.parseInt(splitLine[1]);
                        } else if (Keywords.fromString(splitLine[0]) == Keywords.HOST ||
                                Keywords.fromString(splitLine[0]) == Keywords.REGION) {
                            nodelistRoot.add(generateNodelistEntry(splitLine, currentZone,
                                    Integer.valueOf(splitLine[1]), null));
                            currentNodelistTree = CurrentNodelistTree.NETWORK;
                            currentNetwork = Integer.parseInt(splitLine[1]);
                        } else {
                            if (currentNodelistTree == CurrentNodelistTree.ZONE) {
                                nodelistRoot.add(generateNodelistEntry(splitLine, currentZone,
                                        currentZone, Integer.valueOf(splitLine[1])));
                            } else if (currentNodelistTree == CurrentNodelistTree.NETWORK) {
                                nodelistRoot.add(generateNodelistEntry(splitLine, currentZone,
                                        currentNetwork, Integer.valueOf(splitLine[1])));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read nodelist", e);
        }

        return nodelistRoot;
    }

    /**
     * Generates {@link NodelistEntry} from a line of the nodelist
     *
     * @param splitLine line of the nodelist
     * @return {@link NodelistEntry} with data from the line
     */
    private NodelistEntry generateNodelistEntry(String[] splitLine, Integer zone, Integer network, Integer node) {
        if (zone == null || network == null) {
            throw new IllegalArgumentException("Zone or network is null");
        }

        if (zone <= 0 || network <= 0) {
            throw new IllegalArgumentException("Zone or network cannot be equal to zero");
        }

        node = node == null ? 0 : node;

        return new NodelistEntry(zone, network, node, Keywords.fromString(splitLine[0]),
                splitLine[2], splitLine[3], splitLine[4], splitLine[5],
                Integer.parseInt(splitLine[6]), Arrays.copyOfRange(splitLine, 7, splitLine.length));
    }

}