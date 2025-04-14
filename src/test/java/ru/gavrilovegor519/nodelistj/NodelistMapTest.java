package ru.gavrilovegor519.nodelistj;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ru.gavrilovegor519.nodelistj.entries.NodelistEntryMap;

public class NodelistMapTest {

    private static NodelistMap nodelist;

    /**
     * Initializes nodelist object with correct path to nodelist.txt file.
     * @throws URISyntaxException thrown if path is incorrect.
     */
    @BeforeAll
    static void setUp() throws URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(NodelistMapTest.class.getResource("/nodelist.txt")).toURI());
        nodelist = new NodelistMap(path);
    }

    /**
     * Checks that nodelist object is not initialized with null path.
     */
    @Test
    void testGetNodelistEntryWithNullAddress() {
        assertThrows(IllegalArgumentException.class, () -> nodelist.getNodelistEntryMap(null));
    }

    /**
     * Checks that nodelist object is not initialized with incorrect path to nodelist.txt file.
     */
    @Test
    void testGetNodelistEntryWithIncorrectAddressFormat() {
        assertThrows(IllegalArgumentException.class, () -> nodelist.getNodelistEntryMap("incorrectAddress"));
    }

    /**
     * Checks that nodelist object is working correctly.
     */
    @Test
    void testGetNodelistEntryWithCorrectAddress() {
        assertDoesNotThrow(() -> nodelist.getNodelistEntryMap("2:5015/519"));
    }

    /**
     * Checks that nodelist object is working correctly.
     */
    @Test
    void testDataFromNodelist() {
        NodelistEntryMap entryDto = nodelist.getNodelistEntryMap("2:5015/519");
        assertNull(entryDto.keywords());
        assertEquals("GavrilovNode", entryDto.nodeName());
        assertEquals("Kstovo_Russia", entryDto.location());
        assertEquals("Egor_Gavrilov", entryDto.sysOpName());
        assertEquals("-Unpublished-", entryDto.phone());
        assertEquals(300, entryDto.baudRate());
        assertArrayEquals(new String[]{"CM", "IBN", "INA:gavrilovegor519.ru"}, entryDto.flags());
    }

    /**
     * Checks that getting zone nodelist data is working correctly.
     */
    @Test
    void testGetZoneNodelistData() {
        assertDoesNotThrow(() -> nodelist.getZoneNodelistEntries(2));
    }

    /**
     * Checks that getting network nodelist data is working correctly.
     */
    @Test
    void testGetNetworkNodelistData() {
        assertDoesNotThrow(() -> nodelist.getNetworkNodelistEntries(2, 5015));
    }

    /**
     * Checks that getting zone nodelist in incorrect zone is throws an exception correctly.
     */
    @Test
    void testGetZoneNodelistWithIncorrectZone() {
        assertThrows(IllegalArgumentException.class, () -> nodelist.getZoneNodelistEntries(0));
    }

    /**
     * Checks that getting network nodelist in incorrect network is throws an exception correctly.
     */
    @Test
    void testGetNetworkNodelistWithIncorrectNetwork() {
        assertThrows(IllegalArgumentException.class, () -> nodelist.getNetworkNodelistEntries(2, -999));
    }

    /**
     * Checks that getting network nodelist in incorrect zone is throws an exception correctly.
     */
    @Test
    void testGetNetworkNodelistWithIncorrectZone() {
        assertThrows(IllegalArgumentException.class, () -> nodelist.getNetworkNodelistEntries(0, 5015));
    }
}
