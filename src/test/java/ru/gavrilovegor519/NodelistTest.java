package ru.gavrilovegor519;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.gavrilovegor519.dto.NodelistEntryDto;

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
        NodelistEntryDto entryDto = nodelist.getNodelistEntry("2:5015/519");
        assertNull(entryDto.keywords());
        assertEquals(519, entryDto.number());
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
}
