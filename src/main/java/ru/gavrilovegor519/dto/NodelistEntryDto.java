package ru.gavrilovegor519.dto;

import ru.gavrilovegor519.enums.Keywords;

import java.util.List;

/**
 * Fidonet nodelist entry.
 */
public record NodelistEntryDto (Keywords keywords, Integer number,
                                String nodeName, String location,
                                String sysOpName, String phone,
                                Integer baudRate, String[] flags,
                                List<NodelistEntryDto> children) { }
