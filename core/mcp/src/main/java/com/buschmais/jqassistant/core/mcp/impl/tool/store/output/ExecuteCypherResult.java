package com.buschmais.jqassistant.core.mcp.impl.tool.store.output;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class ExecuteCypherResult {

    public enum Status {
        SUCCESS,
        ERROR
    }

    private Status status;

    private int rowCount;

    private boolean truncated;

    private List<Map<String, Object>> rows;

    private String errorMessage;

}
