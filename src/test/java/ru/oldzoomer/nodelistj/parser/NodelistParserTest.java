package ru.oldzoomer.nodelistj.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import ru.oldzoomer.nodelistj.entries.NodelistEntry;
import ru.oldzoomer.nodelistj.enums.Keywords;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NodelistParser}.
 */
class NodelistParserTest {

    // ─── Real nodelist ─────────────────────────────────────────────────

    @Test
    @DisplayName("parse real nodelist.txt — should produce non-empty list with valid entries")
    void parseRealNodelist_producesNonEmptyList() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("nodelist.txt")) {
            assertNotNull(is, "nodelist.txt must be present in test resources");

            List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

            assertNotNull(entries);
            assertFalse(entries.isEmpty(), "Should parse at least some entries");
            assertTrue(entries.size() > 100, "Real nodelist should yield many entries");
        }
    }

    @Test
    @DisplayName("parse real nodelist — first entry is Zone 1")
    void parseRealNodelist_firstEntryIsZone1() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("nodelist.txt")) {
            List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

            NodelistEntry first = entries.get(0);
            assertEquals(Keywords.ZONE, first.keywords());
            assertEquals(1, first.zone());
            assertEquals(1, first.network());
            assertEquals(0, first.node());            // Zone nodes have node=0
            assertEquals("North_America_(065)", first.nodeName());
            assertEquals("Toronto", first.location());
            assertEquals("Nick_Andre", first.sysOpName());
            assertEquals("1-647-847-2083", first.phone());
            assertEquals(9600, first.baudRate());
        }
    }

    @Test
    @DisplayName("parse real nodelist — children of Zone 1 inherit zone and network")
    void parseRealNodelist_childrenInheritZoneContext() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("nodelist.txt")) {
            List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

            // Entries before second "Zone,2" should be in zone=1
            NodelistEntry beforeZone2 = null;
            for (NodelistEntry e : entries) {
                if (e.keywords() == Keywords.ZONE && e.zone() == 2) {
                    break;
                }
                if (e.keywords() != Keywords.ZONE) {
                    assertEquals(1, e.zone(), "All entries before Zone 2 must be in zone 1");
                }
                beforeZone2 = e;
            }
            assertNotNull(beforeZone2, "Should exist entries before Zone 2");
        }
    }

    @Test
    @DisplayName("parse real nodelist — Zone 2 entries have correct zone")
    void parseRealNodelist_zone2EntriesHaveCorrectZone() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("nodelist.txt")) {
            List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

            // Find index of Zone,2
            int zone2Idx = -1;
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).keywords() == Keywords.ZONE && entries.get(i).zone() == 2) {
                    zone2Idx = i;
                    break;
                }
            }
            assertTrue(zone2Idx >= 0, "Zone 2 should be present in the nodelist");

            // All entries after Zone,2 until the next Zone entry must have zone=2
            for (int i = zone2Idx + 1; i < entries.size(); i++) {
                NodelistEntry e = entries.get(i);
                if (e.keywords() == Keywords.ZONE) {
                    break; // next zone starts, stop checking
                }
                assertEquals(2, e.zone(),
                        "Entry at index " + i + " after Zone 2 must be in zone 2: " + e);
            }
        }
    }

    @Test
    @DisplayName("parse real nodelist — all entries have non-null fields")
    void parseRealNodelist_allEntriesHaveNonNullFields() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("nodelist.txt")) {
            List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

            for (NodelistEntry e : entries) {
                assertNotNull(e.zone(), "zone must not be null");
                assertNotNull(e.network(), "network must not be null");
                assertNotNull(e.node(), "node must not be null");
                // keywords can be null for entries with empty keyword (line starts with comma)
                assertNotNull(e.nodeName(), "nodeName must not be null");
                assertNotNull(e.location(), "location must not be null");
                assertNotNull(e.sysOpName(), "sysOpName must not be null");
                assertNotNull(e.phone(), "phone must not be null");
                assertNotNull(e.baudRate(), "baudRate must not be null");
                assertNotNull(e.flags(), "flags must not be null");
            }
        }
    }

    // ─── Empty / comment-only input ────────────────────────────────────

    @Test
    @DisplayName("empty input returns empty list")
    void emptyInput_returnsEmptyList() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
    @DisplayName("comment-only input returns empty list")
    void commentOnlyInput_returnsEmptyList() throws IOException {
        String input = ";A comment line\n;S Another comment\n;  \n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
    @DisplayName("blank-line-only input returns empty list")
    void blankLinesOnly_returnsEmptyList() throws IOException {
        String input = "\n  \n\t\n\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
    @DisplayName("mixed comments and blanks returns empty list")
    void mixedCommentsAndBlanks_returnsEmptyList() throws IOException {
        String input = ";comment\n  \n;another\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);
        assertTrue(entries.isEmpty());
    }

    // ─── Zone parsing ──────────────────────────────────────────────────

    @Test
    @DisplayName("standalone Zone line without following entries is parsed")
    void standaloneZone_parsed() throws IOException {
        String input = "Zone,1,Test_Zone,City,Sysop,Phone,300\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(1, entries.size());
        NodelistEntry e = entries.get(0);
        assertEquals(Keywords.ZONE, e.keywords());
        assertEquals(1, e.zone());
        assertEquals(1, e.network());
        assertEquals(0, e.node());         // Zone nodes have node=0
        assertEquals("Test_Zone", e.nodeName());
    }

    @Test
    @DisplayName("Zone sets context for subsequent entries")
    void zoneSetsContext() throws IOException {
        String input = "Zone,5,MyZone,City,Sysop,Phone,300\n"
                     + "Pvt,100,TestNode,City,Sysop,Phone,9600\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(2, entries.size());
        // Zone entry
        assertEquals(5, entries.get(0).zone());
        assertEquals(Keywords.ZONE, entries.get(0).keywords());
        // Pvt child — inherits zone from context
        assertEquals(5, entries.get(1).zone());
        assertEquals(Keywords.PVT, entries.get(1).keywords());
        assertEquals(100, entries.get(1).node());
    }

    @Test
    @DisplayName("line without prior Zone/Region/Host is skipped (no context)")
    void lineWithoutPriorContext_isSkipped() throws IOException {
        // A Pvt line without a preceding Zone/Host/Region has no zone/network context
        String input = "Pvt,100,TestNode,City,Sysop,Phone,9600\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertTrue(entries.isEmpty(),
                "Entry without zone/network context should be skipped");
    }

    // ─── Host / Region parsing ─────────────────────────────────────────

    @Test
    @DisplayName("Host sets network context for subsequent entries")
    void hostSetsNetworkContext() throws IOException {
        String input = "Zone,1,Z,S,S,P,300\n"
                     + "Host,102,MyNet,City,Sysop,Phone,300\n"
                     + "Pvt,10,MyNode,City,Sysop,Phone,9600\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(3, entries.size());
        // Host entry
        assertEquals(Keywords.HOST, entries.get(1).keywords());
        assertEquals(0, entries.get(1).node());         // Host node=0
        assertEquals(102, entries.get(1).network());
        // Child of Host — inherits network=102, zone=1
        NodelistEntry child = entries.get(2);
        assertEquals(1, child.zone());
        assertEquals(102, child.network());
        assertEquals(10, child.node());
        assertEquals(Keywords.PVT, child.keywords());
    }

    @Test
    @DisplayName("Region sets network context (same as Host)")
    void regionSetsNetworkContext() throws IOException {
        String input = "Zone,1,Z,S,S,P,300\n"
                     + "Region,20,Scandinavia,City,Sysop,Phone,300\n"
                     + "Host,201,Capital_Net,City,Sysop,Phone,300\n"
                     + ",111,Capital_City,City,Sysop,Phone,9600\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(4, entries.size());
        // Region entry
        assertEquals(Keywords.REGION, entries.get(1).keywords());
        assertEquals(0, entries.get(1).node());
        assertEquals(20, entries.get(1).network());

        // Host under Region — network=201, zone=1
        assertEquals(Keywords.HOST, entries.get(2).keywords());
        assertEquals(1, entries.get(2).zone());
        assertEquals(201, entries.get(2).network());
        assertEquals(0, entries.get(2).node());         // Host node=0

        // Child of Host (empty keyword line) — zone=1, network=201
        assertEquals(1, entries.get(3).zone());
        assertEquals(201, entries.get(3).network());
        assertEquals(111, entries.get(3).node());
    }

    // ─── All keyword types ─────────────────────────────────────────────

    @Test
    @DisplayName("all keyword types are parsed correctly")
    void allKeywordTypes_parsed() throws IOException {
        String input = "Zone,1,Z,S,S,P,300\n"
                     + "Host,10,HNet,City,Sysop,Phone,300\n"
                     + "Hub,1,HubNode,City,Sysop,Phone,300,FLAG\n"
                     + "Pvt,100,PvtNode,City,Sysop,Phone,9600\n"
                     + "Hold,200,HoldNode,City,Sysop,Phone,300\n"
                     + "Down,300,DownNode,City,Sysop,Phone,300\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(6, entries.size());
        assertEquals(Keywords.ZONE, entries.get(0).keywords());
        assertEquals(Keywords.HOST, entries.get(1).keywords());
        assertEquals(Keywords.HUB,  entries.get(2).keywords());
        assertEquals(Keywords.PVT,  entries.get(3).keywords());
        assertEquals(Keywords.HOLD, entries.get(4).keywords());
        assertEquals(Keywords.DOWN, entries.get(5).keywords());
    }

    @Test
    @DisplayName("Hub keyword preserves node number (not 0)")
    void hubPreservesNodeNumber() throws IOException {
        String input = "Zone,1,Z,S,S,P,300\n"
                     + "Host,10,HNet,City,Sysop,Phone,300\n"
                     + "Hub,500,HNode,City,Sysop,Phone,300,XA\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        NodelistEntry hub = entries.get(2);
        assertEquals(Keywords.HUB, hub.keywords());
        assertEquals(500, hub.node());
    }

    // ─── Empty keyword (line starts with comma) ────────────────────────

    @Test
    @DisplayName("line starting with comma gets keyword null from fromString")
    void emptyKeywordLine_parsed() throws IOException {
        // Preprocessed to "###,1,Test,City,Sysop,Phone,300"
        // fromString("###") returns null, keyword is null
        // buildEntry should handle null keyword
        String input = "Zone,1,Z,S,S,P,300\n"
                     + ",1,TestNode,City,Sysop,Phone,9600\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(2, entries.size());
        NodelistEntry child = entries.get(1);
        assertNull(child.keywords(), "Empty keyword entries should have keywords=null");
        assertEquals(1, child.zone());
        assertEquals(1, child.network());
        assertEquals(1, child.node());
        assertEquals("TestNode", child.nodeName());
    }

    // ─── Flags ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("flags beyond 7 fields are preserved in entry")
    void flagsArePreserved() throws IOException {
        String input = "Zone,1,Z,S,S,P,300,CM,XX,INA:bbs.test.org,IBN\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(1, entries.size());
        NodelistEntry e = entries.get(0);
        assertEquals(4, e.flags().length);
        assertEquals("CM", e.flags()[0]);
        assertEquals("XX", e.flags()[1]);
        assertEquals("INA:bbs.test.org", e.flags()[2]);
        assertEquals("IBN", e.flags()[3]);
    }

    @Test
    @DisplayName("no flags — empty array")
    void noFlags_emptyArray() throws IOException {
        String input = "Zone,1,Z,S,S,P,300\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(1, entries.size());
        assertEquals(0, entries.get(0).flags().length);
    }

    // ─── Error handling / edge cases ───────────────────────────────────

    @Test
    @DisplayName("line with too few fields is skipped")
    void lineWithTooFewFields_skipped() throws IOException {
        String input = "Zone,1,Z,S,S,P\n"  // 6 fields, need 7
                     + "Zone,1,Z,S,S,P,300\n"; // valid
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(1, entries.size(),
                "Only the line with 7+ fields should be parsed");
    }

    @Test
    @DisplayName("line with non-numeric field[1] (nodeNumber) is skipped")
    void lineWithNonNumericNodeNumber_skipped() throws IOException {
        String input = "Zone,1,Z,S,S,P,300\n"
                     + "Pvt,abc,Test,City,Sysop,Phone,300\n"; // non-numeric node
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(1, entries.size(), "Entry with non-numeric node should be skipped");
    }

    @Test
    @DisplayName("line with non-numeric baudRate is skipped")
    void lineWithNonNumericBaudRate_skipped() throws IOException {
        String input = "Zone,1,Z,S,S,P,300\n"
                     + "Pvt,100,Test,City,Sysop,Phone,abc\n"; // non-numeric baud
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(1, entries.size(), "Entry with non-numeric baudRate should be skipped");
    }

    @Test
    @DisplayName("nodes after Zone/Host/Region have node=0")
    void structuralNodes_haveNodeZero() throws IOException {
        String input = "Zone,2,Europe,City,Sysop,Phone,300\n"
                     + "Region,20,Scandinavia,City,Sysop,Phone,300\n"
                     + "Host,201,Capital,City,Sysop,Phone,300\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(3, entries.size());
        assertEquals(0, entries.get(0).node(), "Zone node=0");
        assertEquals(0, entries.get(1).node(), "Region node=0");
        assertEquals(0, entries.get(2).node(), "Host node=0");
    }

    @Test
    @DisplayName("field separator only at start — preprocessed with ###")
    void fieldStartsWithCommaPreprocessing() throws IOException {
        // The actual nodelist format: ,number,name,...
        // Preprocessing adds "###" prefix
        String input = "Zone,1,Z,S,S,P,300\n"
                     + ",42,Test,City,Sysop,Phone,9600,FLAG1,FLAG2\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(2, entries.size());
        NodelistEntry child = entries.get(1);
        assertNull(child.keywords());
        assertEquals(42, child.node());
        assertArrayEquals(new String[]{"FLAG1", "FLAG2"}, child.flags());
    }

    // ─── Zone transition resets network ────────────────────────────────

    @Test
    @DisplayName("new Zone resets both zone and network")
    void newZoneResetsContext() throws IOException {
        String input = "Zone,1,Z1,S,S,P,300\n"
                     + "Host,10,H1,S,S,P,300\n"
                     + "Pvt,5,N1,S,S,P,300\n"
                     + "Zone,2,Z2,S,S,P,300\n"
                     + "Host,20,H2,S,S,P,300\n"
                     + "Pvt,15,N2,S,S,P,300\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(6, entries.size());

        // Zone 1 group
        assertEquals(1, entries.get(0).zone()); // Zone,1
        assertEquals(1, entries.get(1).zone()); // Host,10 in zone 1
        assertEquals(10, entries.get(1).network());
        assertEquals(1, entries.get(2).zone()); // Pvt,5 in zone 1
        assertEquals(10, entries.get(2).network());

        // Zone 2 group
        assertEquals(2, entries.get(3).zone()); // Zone,2
        assertEquals(2, entries.get(4).zone()); // Host,20 in zone 2
        assertEquals(20, entries.get(4).network());
        assertEquals(2, entries.get(5).zone()); // Pvt,15 in zone 2
        assertEquals(20, entries.get(5).network());
    }

    // ─── New Host/Region changes network within same zone ──────────────

    @Test
    @DisplayName("Host after Host changes network, same zone")
    void hostAfterHostChangesNetwork() throws IOException {
        String input = "Zone,1,Z,S,S,P,300\n"
                     + "Host,10,Net10,S,S,P,300\n"
                     + "Pvt,1,N1,S,S,P,300\n"
                     + "Host,20,Net20,S,S,P,300\n"
                     + "Pvt,2,N2,S,S,P,300\n";
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        List<NodelistEntry> entries = NodelistParser.parseNodelist(is);

        assertEquals(5, entries.size());

        // N1 under Net10
        assertEquals(1, entries.get(2).zone());
        assertEquals(10, entries.get(2).network());

        // N2 under Net20
        assertEquals(1, entries.get(4).zone());
        assertEquals(20, entries.get(4).network());
    }
}
