package ru.oldzoomer.nodelistj.entries;

import ru.oldzoomer.nodelistj.enums.Keywords;

/**
 * Common sealed interface for Fidonet nodelist entries.
 * Both flat ({@link NodelistEntry}) and hierarchical ({@link NodelistEntryMap})
 * entries share these core fields.
 */
public sealed interface BaseEntry permits NodelistEntry, NodelistEntryMap {

    Keywords keywords();

    String nodeName();

    String location();

    String sysOpName();

    String phone();

    Integer baudRate();

    String[] flags();
}
