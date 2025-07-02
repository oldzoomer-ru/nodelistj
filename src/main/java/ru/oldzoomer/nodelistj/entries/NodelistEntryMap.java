package ru.oldzoomer.nodelistj.entries;

import java.util.Map;

import ru.oldzoomer.nodelistj.enums.Keywords;

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
 * @param map       entry node children
 */
public record NodelistEntryMap(Keywords keywords, String nodeName, String location,
                            String sysOpName, String phone, Integer baudRate,
                            String[] flags, Map<Integer, NodelistEntryMap> children) {
}