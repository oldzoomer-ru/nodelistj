package ru.oldzoomer.nodelistj.parser;

import ru.oldzoomer.nodelistj.entries.NodelistEntry;
import ru.oldzoomer.nodelistj.enums.Keywords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a Fidonet nodelist from an {@link InputStream} into a flat list of {@link NodelistEntry} records.
 *
 * <p>Delegates shared parsing utilities to {@link ParserUtils}.</p>
 */
public final class NodelistParser {

    private NodelistParser() {
    }

    /**
     * Parses a nodelist into a flat list of entries.
     *
     * @param inputStream the source stream (not closed by this method; caller is responsible)
     * @return list of parsed {@link NodelistEntry} records
     * @throws IOException if an I/O error occurs while reading
     */
    public static List<NodelistEntry> parseNodelist(InputStream inputStream) throws IOException {
        List<NodelistEntry> entries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            ParserUtils.ParsingContext ctx = new ParserUtils.ParsingContext();
            String line;

            while ((line = reader.readLine()) != null) {
                if (ParserUtils.shouldSkipLine(line)) {
                    continue;
                }

                NodelistEntry entry = parseLine(line, ctx);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }

        return entries;
    }

    private static NodelistEntry parseLine(String line, ParserUtils.ParsingContext ctx) {
        String processed = ParserUtils.preprocessLine(line);
        String[] fields = processed.split(ParserUtils.FIELD_SEPARATOR, -1);

        if (fields.length < ParserUtils.MIN_FIELDS_REQUIRED) {
            return null;
        }

        Keywords keyword = Keywords.fromString(fields[0]);
        return buildEntry(fields, keyword, ctx);
    }

    private static NodelistEntry buildEntry(String[] fields, Keywords keyword, ParserUtils.ParsingContext ctx) {
        try {
            Integer nodeNumber = ParserUtils.parseInteger(fields[1]);
            if (nodeNumber == null) {
                return null;
            }

            updateContext(ctx, keyword, nodeNumber);

            Integer zone = ctx.getCurrentZone();
            Integer network = ctx.getCurrentNetwork();
            Integer node = resolveNode(keyword, nodeNumber);

            if (zone == null || network == null) {
                return null;
            }

            Integer baudRate = ParserUtils.parseInteger(fields[6]);
            if (baudRate == null) {
                return null;
            }

            return new NodelistEntry(
                    zone, network, node, keyword,
                    fields[2], fields[3], fields[4], fields[5],
                    baudRate,
                    ParserUtils.extractFlags(fields)
            );
        } catch (Exception e) {
            return null; // skip malformed entries
        }
    }

    private static void updateContext(ParserUtils.ParsingContext ctx, Keywords keyword, Integer nodeNumber) {
        if (keyword == Keywords.ZONE) {
            ctx.setCurrentZone(nodeNumber);
            ctx.setCurrentNetwork(nodeNumber);
            ctx.setCurrentTree(ParserUtils.ParsingContext.TreeLevel.ZONE);
        } else if (keyword == Keywords.HOST || keyword == Keywords.REGION) {
            ctx.setCurrentNetwork(nodeNumber);
            ctx.setCurrentTree(ParserUtils.ParsingContext.TreeLevel.NETWORK);
        }
    }

    private static Integer resolveNode(Keywords keyword, Integer nodeNumber) {
        if (keyword == Keywords.ZONE || keyword == Keywords.HOST || keyword == Keywords.REGION) {
            return 0;
        }
        return nodeNumber;
    }
}