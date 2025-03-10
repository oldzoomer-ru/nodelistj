package ru.gavrilovegor519.dto;

import ru.gavrilovegor519.enums.Keywords;

import java.util.Map;

/**
 * Fidonet nodelist entry.
 */
public record NodelistEntryDto (Keywords keywords, String nodeName, String location,
                                String sysOpName, String phone, Integer baudRate,
                                String[] flags, Map<Integer, NodelistEntryDto> children) { }
