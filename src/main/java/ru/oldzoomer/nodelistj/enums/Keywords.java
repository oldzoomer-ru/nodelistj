package ru.oldzoomer.nodelistj.enums;

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
    DOWN("Down");

    private final String keyword;

    Keywords(String keyword) {
        this.keyword = keyword;
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
        return null;
    }

    /**
     * Get keyword value
     * @return keyword value
     */
    @Override
    public String toString() {
        return keyword;
    }
}