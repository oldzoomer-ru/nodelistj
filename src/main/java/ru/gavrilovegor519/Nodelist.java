package ru.gavrilovegor519;

import ru.gavrilovegor519.dto.NodelistEntryDto;
import ru.gavrilovegor519.enums.CurrentNodelistTree;
import ru.gavrilovegor519.enums.Keywords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Fidonet Nodelist parser
 */
public class Nodelist {

    private final List<NodelistEntryDto> nodelistEntries = new ArrayList<>();

    public Nodelist(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path is null");
        }

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("File does not exist");
        }

        try {
            indexNodelist(new InputStreamReader(Files.newInputStream(path)));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read file", e);
        }
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

        if (!address.matches("^[0-9]+:[0-9]+/[0-9]+$")) {
            throw new IllegalArgumentException("Incorrect address format");
        }

        int zone = Integer.parseInt(address.substring(0, address.indexOf(":")));
        int network = Integer.parseInt(address.substring(address.indexOf(":") + 1, address.indexOf("/")));
        int node = Integer.parseInt(address.substring(address.indexOf("/") + 1));

        return nodelistEntries.stream()
                .filter(nodelistEntryDto -> nodelistEntryDto.getNumber() == zone)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Zone not found"))
                .getChildren().stream()
                .filter(nodelistEntryDto -> nodelistEntryDto.getNumber() == network)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Network not found"))
                .getChildren().stream()
                .filter(nodelistEntryDto -> nodelistEntryDto.getNumber() == node)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Node not found"));
    }

    /**
     * Reads Fidonet nodelist and indexes it in memory
     *
     * @param reader Nodelist file reader
     */
    private void indexNodelist(InputStreamReader reader) {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            CurrentNodelistTree currentKeyword = null;

            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();

                if (line.startsWith(";") || line.isBlank()) continue;

                if (line.startsWith(",")) line = "###" + line; // Fixes an issue with the empty keyword at the beginning of the nodelist

                String[] splitLine = line.split(",");

                if (splitLine.length < 7) continue;

                if (Keywords.fromString(splitLine[0]) == Keywords.ZONE) {
                    nodelistEntries.add(generateNodelistEntry(splitLine));
                    currentKeyword = CurrentNodelistTree.ZONE;
                } else if (Keywords.fromString(splitLine[0]) == Keywords.HOST || Keywords.fromString(splitLine[0]) == Keywords.REGION) {
                    nodelistEntries.getLast().getChildren().add(generateNodelistEntry(splitLine));
                    currentKeyword = CurrentNodelistTree.NETWORK;
                } else {
                    if (currentKeyword == CurrentNodelistTree.ZONE) {
                        nodelistEntries.getLast().getChildren().add(generateNodelistEntry(splitLine));
                    } else if (currentKeyword == CurrentNodelistTree.NETWORK) {
                        nodelistEntries.getLast().getChildren().getLast().getChildren().add(generateNodelistEntry(splitLine));
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
        return NodelistEntryDto.builder()
                .keywords(Keywords.fromString(splitLine[0]))
                .number(Integer.parseInt(splitLine[1]))
                .nodeName(splitLine[2])
                .location(splitLine[3])
                .sysOpName(splitLine[4])
                .phone(splitLine[5])
                .baudRate(Integer.parseInt(splitLine[6]))
                .flags(Arrays.copyOf(splitLine, 7))
                .children(new ArrayList<>())
                .build();
    }

}
