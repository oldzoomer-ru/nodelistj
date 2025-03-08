package ru.gavrilovegor519;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class NodelistTest {

    private Nodelist nodelist;

    /**
     * Initializes nodelist object with correct path to nodelist.txt file.
     * @throws URISyntaxException thrown if path is incorrect.
     */
    @BeforeEach
    void setUp() throws URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(getClass().getResource("/nodelist.txt")).toURI());
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
        assertDoesNotThrow(() -> nodelist.getNodelistEntry("2:5015/46"));
    }
}
