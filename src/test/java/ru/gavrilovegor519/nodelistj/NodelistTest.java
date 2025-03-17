package ru.gavrilovegor519.nodelistj;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.gavrilovegor519.nodelistj.entries.NodelistEntry;
import ru.gavrilovegor519.nodelistj.enums.Keywords;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class NodelistTest {

    private static Nodelist nodelist;

    /**
     * Initializes nodelist object with correct path to nodelist.txt file.
     * @throws URISyntaxException thrown if path is incorrect.
     */
    @BeforeAll
    static void setUp() throws URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(NodelistTest.class.getResource("/nodelist.txt")).toURI());
        nodelist = new Nodelist(path);
    }

    /**
     * Checks that nodelist object is not initialized with null path.
     */
    @Test
    void testGetNodelistEntryWithNullAddress() {
        assertThrows(IllegalArgumentException.class, () -> nodelist.getNodelistEntry(null));
    }

    /**
     * Checks that nodelist object is not initialized with incorrect path to nodelist.txt file.
     */
    @Test
    void testGetNodelistEntryWithIncorrectAddressFormat() {
        assertThrows(IllegalArgumentException.class, () -> nodelist.getNodelistEntry("incorrectAddress"));
    }

    /**
     * Checks that nodelist object is working correctly.
     */
    @Test
    void testGetNodelistEntryWithCorrectAddress() {
        assertDoesNotThrow(() -> nodelist.getNodelistEntry("2:5015/519"));
    }

    /**
     * Checks that nodelist object is working correctly.
     */
    @Test
    void testDataFromNodelist() {
        NodelistEntry entryDto = nodelist.getNodelistEntry("2:5015/519");
        assertEquals(2, entryDto.zone());
        assertEquals(5015, entryDto.network());
        assertEquals(519, entryDto.node());
        assertNull(entryDto.keywords());
        assertEquals("GavrilovNode", entryDto.nodeName());
        assertEquals("Kstovo_Russia", entryDto.location());
        assertEquals("Egor_Gavrilov", entryDto.sysOpName());
        assertEquals("-Unpublished-", entryDto.phone());
        assertEquals(300, entryDto.baudRate());
        assertArrayEquals(new String[]{"CM", "IBN", "INA:gavrilovegor519.ru"}, entryDto.flags());
    }

    /**
     * Checks that nodelist object is working correctly.
     */
    @Test
    void testDataFromNodelist2() {
        NodelistEntry entryDto = nodelist.getNodelistEntry("2:2/0");
        assertEquals(2, entryDto.zone());
        assertEquals(2, entryDto.network());
        assertEquals(0, entryDto.node());
        System.out.println(entryDto);
    }

    /**
     * Checks that nodelist object is working correctly.
     */
    @Test
    void testDataFromNodelist3() {
        NodelistEntry entryDto = nodelist.getNodelistEntry("2:2/2");
        assertEquals(2, entryDto.zone());
        assertEquals(2, entryDto.network());
        assertEquals(2, entryDto.node());
        System.out.println(entryDto);
    }

    /**
     * Checks that nodelist object is working correctly.
     */
    @Test
    void testDataFromNodelist4() {
        NodelistEntry entryDto = nodelist.getNodelistEntry("1:1/0");
        assertEquals(1, entryDto.zone());
        assertEquals(1, entryDto.network());
        assertEquals(0, entryDto.node());
        assertEquals(Keywords.ZONE, entryDto.keywords());
        System.out.println(entryDto);
    }

    /**
     * Checks that getting zone nodelist data is working correctly.
     */
    @Test
    void testGetZoneNodelistData() {
        assertDoesNotThrow(() -> nodelist.getNodelistEntries(2));
    }

    /**
     * Checks that getting network nodelist data is working correctly.
     */
    @Test
    void testGetNetworkNodelistData() {
        assertDoesNotThrow(() -> nodelist.getNodelistEntries(2, 5015));
    }

    /**
     * Checks that getting zone nodelist in incorrect zone is throws an exception correctly.
     */
    @Test
    void testGetZoneNodelistWithIncorrectZone() {
        assertThrows(IllegalArgumentException.class, () -> nodelist.getNodelistEntries(0));
    }

    /**
     * Checks that getting network nodelist in incorrect network is throws an exception correctly.
     */
    @Test
    void testGetNetworkNodelistWithIncorrectNetwork() {
        assertThrows(IllegalArgumentException.class, () -> nodelist.getNodelistEntries(2, -999));
    }

    /**
     * Checks that getting network nodelist in incorrect zone is throws an exception correctly.
     */
    @Test
    void testGetNetworkNodelistWithIncorrectZone() {
        assertThrows(IllegalArgumentException.class, () -> nodelist.getNodelistEntries(0, 5015));
    }
}
