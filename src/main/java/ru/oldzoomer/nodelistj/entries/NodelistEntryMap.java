package ru.oldzoomer.nodelistj.entries;

import ru.oldzoomer.nodelistj.enums.Keywords;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Fidonet nodelist entry.
 *
 * @param keywords  entry keywords
 * @param nodeName  entry node name
 * @param location  entry node location
 * @param sysOpName entry node sysop name
 * @param phone     entry node phone number
 * @param baudRate  entry node baud rate
 * @param flags     entry node flags
 */
public record NodelistEntryMap(Keywords keywords, String nodeName, String location,
                            String sysOpName, String phone, Integer baudRate,
                            String[] flags, Map<Integer, NodelistEntryMap> children) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NodelistEntryMap(
                Keywords keywords1, String name, String location1, String opName, String phone1, Integer rate,
                String[] flags1, Map<Integer, NodelistEntryMap> children1
        ))) return false;
        return Objects.equals(phone, phone1) &&
                Objects.deepEquals(flags, flags1) &&
                Objects.equals(nodeName, name) &&
                Objects.equals(location, location1) &&
                Objects.equals(sysOpName, opName) &&
                Objects.equals(baudRate, rate) &&
                keywords == keywords1 &&
                Objects.equals(children, children1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keywords, nodeName, location, sysOpName,
                phone, baudRate, Arrays.hashCode(flags), children);
    }

    @Override
    public String toString() {
        return "NodelistEntryMap{" +
                "keywords=" + keywords +
                ", nodeName='" + nodeName + '\'' +
                ", location='" + location + '\'' +
                ", sysOpName='" + sysOpName + '\'' +
                ", phone='" + phone + '\'' +
                ", baudRate=" + baudRate +
                ", flags=" + Arrays.toString(flags) +
                ", children=" + children +
                '}';
    }
}