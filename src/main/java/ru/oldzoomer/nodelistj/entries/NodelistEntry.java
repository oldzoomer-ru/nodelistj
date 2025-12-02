package ru.oldzoomer.nodelistj.entries;

import ru.oldzoomer.nodelistj.enums.Keywords;

import java.util.Arrays;
import java.util.Objects;

/**
 * Fidonet nodelist entry.
 *
 * @param zone      entry node zone
 * @param network   entry node network
 * @param node      entry node number
 * @param keywords  entry keywords
 * @param nodeName  entry node name
 * @param location  entry node location
 * @param sysOpName entry node sysop name
 * @param phone     entry node phone number
 * @param baudRate  entry node baud rate
 * @param flags     entry node flags
 */
public record NodelistEntry(Integer zone, Integer network, Integer node,
                            Keywords keywords, String nodeName, String location,
                            String sysOpName, String phone, Integer baudRate,
                            String[] flags) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NodelistEntry(
                Integer zone1, Integer network1, Integer node1, Keywords keywords1, String name, String location1,
                String opName, String phone1, Integer rate, String[] flags1
        ))) return false;
        return Objects.equals(zone, zone1) && Objects.equals(node, node1) && Objects.equals(phone, phone1) &&
                Objects.deepEquals(flags, flags1) && Objects.equals(network, network1) &&
                Objects.equals(nodeName, name) && Objects.equals(location, location1) &&
                Objects.equals(sysOpName, opName) && Objects.equals(baudRate, rate) && keywords == keywords1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(zone, network, node, keywords,
                nodeName, location, sysOpName, phone,
                baudRate, Arrays.hashCode(flags));
    }

    @Override
    public String toString() {
        return "NodelistEntry{" +
                "zone=" + zone +
                ", network=" + network +
                ", node=" + node +
                ", keywords=" + keywords +
                ", nodeName='" + nodeName + '\'' +
                ", location='" + location + '\'' +
                ", sysOpName='" + sysOpName + '\'' +
                ", phone='" + phone + '\'' +
                ", baudRate=" + baudRate +
                ", flags=" + Arrays.toString(flags) +
                '}';
    }
}