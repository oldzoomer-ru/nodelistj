package ru.oldzoomer.nodelistj.parser;

import ru.oldzoomer.nodelistj.entries.NodelistEntryMap;
import ru.oldzoomer.nodelistj.enums.Keywords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses a Fidonet nodelist from an {@link InputStream} into a hierarchical map structure
 * of {@link NodelistEntryMap} records.
 *
 * <p>Delegates shared parsing utilities to {@link ParserUtils} to avoid duplication
 * with {@link NodelistParser}.</p>
 */
public final class NodelistMapParser {

    private NodelistMapParser() {
    }

    /**
     * Parses a nodelist into a hierarchical map: zone → network → node.
     *
     * @param inputStream the source stream (not closed by this method; caller is responsible)
     * @return map of zone numbers to their {@link NodelistEntryMap} entries
     * @throws IOException if an I/O error occurs while reading
     */
    public static Map<Integer, NodelistEntryMap> parseNodelistMap(InputStream inputStream) throws IOException {
        Map<Integer, NodelistEntryMap> nodelistEntries = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            ParserUtils.ParsingContext ctx = new ParserUtils.ParsingContext();
            String line;

            while ((line = reader.readLine()) != null) {
                if (ParserUtils.shouldSkipLine(line)) {
                    continue;
                }
                processLine(line, nodelistEntries, ctx);
            }
        }

        return nodelistEntries;
    }

    private static void processLine(String line, Map<Integer, NodelistEntryMap> entries,
                                    ParserUtils.ParsingContext ctx) {
        String processed = ParserUtils.preprocessLine(line);
        String[] fields = processed.split(ParserUtils.FIELD_SEPARATOR, -1);

        if (fields.length < ParserUtils.MIN_FIELDS_REQUIRED) {
            return;
        }

        Keywords keyword = Keywords.fromString(fields[0]);

        try {
            Integer nodeNumber = ParserUtils.parseInteger(fields[1]);
            if (nodeNumber == null) {
                return;
            }

            NodelistEntryMap entry = new NodelistEntryMap(
                    keyword, fields[2], fields[3], fields[4], fields[5],
                    parseBaudRate(fields[6]),
                    ParserUtils.extractFlags(fields),
                    new HashMap<>()
            );

            insertIntoMap(entries, entry, keyword, nodeNumber, ctx);
        } catch (Exception e) {
            // skip malformed entries
        }
    }

    private static void insertIntoMap(Map<Integer, NodelistEntryMap> entries,
                                      NodelistEntryMap entry, Keywords keyword,
                                      Integer nodeNumber, ParserUtils.ParsingContext ctx) {
        if (keyword == Keywords.ZONE) {
            entries.put(nodeNumber, entry);
            ctx.setCurrentZone(nodeNumber);
            ctx.setCurrentTree(ParserUtils.ParsingContext.TreeLevel.ZONE);
        } else if (keyword == Keywords.HOST || keyword == Keywords.REGION) {
            if (ctx.getCurrentZone() != null && entries.containsKey(ctx.getCurrentZone())) {
                entries.get(ctx.getCurrentZone()).children().put(nodeNumber, entry);
                ctx.setCurrentNetwork(nodeNumber);
                ctx.setCurrentTree(ParserUtils.ParsingContext.TreeLevel.NETWORK);
            }
        } else {
            // Regular node — place under current zone or network
            if (ctx.getCurrentZone() == null || !entries.containsKey(ctx.getCurrentZone())) {
                return;
            }
            if (ctx.getCurrentTree() == ParserUtils.ParsingContext.TreeLevel.ZONE) {
                entries.get(ctx.getCurrentZone()).children().put(nodeNumber, entry);
            } else if (ctx.getCurrentTree() == ParserUtils.ParsingContext.TreeLevel.NETWORK
                    && ctx.getCurrentNetwork() != null) {
                NodelistEntryMap networkEntry = entries.get(ctx.getCurrentZone())
                        .children()
                        .get(ctx.getCurrentNetwork());
                if (networkEntry != null) {
                    networkEntry.children().put(nodeNumber, entry);
                }
            }
        }
    }

    private static Integer parseBaudRate(String value) {
        Integer rate = ParserUtils.parseInteger(value);
        return rate != null ? rate : 0;
    }
}