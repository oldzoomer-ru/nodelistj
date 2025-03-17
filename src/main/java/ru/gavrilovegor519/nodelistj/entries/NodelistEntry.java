package ru.gavrilovegor519.nodelistj.entries;

import ru.gavrilovegor519.nodelistj.enums.Keywords;

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
}
