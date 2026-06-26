package ru.oldzoomer.nodelistj.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Fidonet nodelist keywords.
 */
public enum Keywords {
    ZONE("Zone"),
    REGION("Region"),
    HOST("Host"),
    HUB("Hub"),
    PVT("Pvt"),
    HOLD("Hold"),
    DOWN("Down");

    private static final Map<String, Keywords> BY_KEYWORD;

    static {
        Map<String, Keywords> map = new HashMap<>();
        for (Keywords k : values()) {
            map.put(k.keyword, k);
        }
        BY_KEYWORD = Collections.unmodifiableMap(map);
    }

    private final String keyword;

    Keywords(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the enum constant for the given keyword string, or {@code null}
     * if no match is found. Lookup is O(1).
     *
     * @param keyword the keyword string (e.g. "Zone", "Host")
     * @return the matching {@link Keywords} or {@code null}
     */
    public static Keywords fromString(String keyword) {
        return BY_KEYWORD.get(keyword);
    }

    /**
     * Returns the keyword string representation.
     *
     * @return the keyword string (e.g. "Zone", "Host")
     */
    @Override
    public String toString() {
        return keyword;
    }
}