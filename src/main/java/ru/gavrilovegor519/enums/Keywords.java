package ru.gavrilovegor519.enums;

/**
 * Fidonet nodelist keywords
 */
public enum Keywords {
    ZONE("Zone"),
    REGION("Region"),
    HOST("Host"),
    HUB("Hub"),
    PVT("Pvt"),
    HOLD("Hold"),
    DOWN("Down"),
    NULL;

    private final String keyword;

    Keywords(String keyword) {
        this.keyword = keyword;
    }

    Keywords() {
        this.keyword = "";
    }

    public String toString() {
        return keyword;
    }

    /**
     * Get enum value by keyword
     * @param keyword keyword
     * @return Keywords enum value
     */
    public static Keywords fromString(String keyword) {
        for (Keywords k : Keywords.values()) {
            if (k.keyword.equals(keyword)) {
                return k;
            }
        }
        return NULL;
    }
}
