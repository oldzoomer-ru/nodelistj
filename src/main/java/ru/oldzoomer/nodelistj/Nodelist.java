package ru.oldzoomer.nodelistj;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import ru.oldzoomer.nodelistj.entries.NodelistEntry;
import ru.oldzoomer.nodelistj.parser.NodelistParser;

/**
 * Optimized Fidonet Nodelist parser with improved performance
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

        try (InputStream inputStream = Files.newInputStream(path)) {
            nodelistRoot = NodelistParser.parseNodelist(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read file", e);
        }
    }

    /**
     * Nodelist constructor with pre-parsed entries
     * @param nodelistRoot pre-parsed nodelist entries
     */
    public Nodelist(List<NodelistEntry> nodelistRoot) {
        if (nodelistRoot == null) {
            throw new IllegalArgumentException("Nodelist entries cannot be null");
        }
        this.nodelistRoot = nodelistRoot;
    }

    /**
     * Nodelist constructor with input stream
     * @param inputStream input stream
     */
    public Nodelist(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        
        try {
            nodelistRoot = NodelistParser.parseNodelist(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse nodelist", e);
        }
    }

    /**
     * Get nodelist entries
     *
     * @return list of nodelist entries
     */
    public List<NodelistEntry> getNodelist() {
        return nodelistRoot;
    }
}