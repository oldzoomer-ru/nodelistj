package ru.oldzoomer.nodelistj.entries;

import ru.oldzoomer.nodelistj.enums.Keywords;

/**
 * Common sealed interface for Fidonet nodelist entries.
 */
public sealed interface BaseEntry permits NodelistEntry {

    Keywords keywords();

    String nodeName();

    String location();

    String sysOpName();

    String phone();

    Integer baudRate();

    String[] flags();
}
