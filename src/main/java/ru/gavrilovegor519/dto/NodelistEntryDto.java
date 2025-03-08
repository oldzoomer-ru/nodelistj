package ru.gavrilovegor519.dto;

import lombok.Builder;
import lombok.Getter;
import ru.gavrilovegor519.enums.Keywords;

import java.util.List;

/**
 * Fidonet nodelist entry.
 */
@Getter
@Builder
public class NodelistEntryDto {
    private Keywords keywords;
    private Integer number;
    private String nodeName;
    private String location;
    private String sysOpName;
    private String phone;
    private Integer baudRate;
    private String[] flags;

    private List<NodelistEntryDto> children;
}
