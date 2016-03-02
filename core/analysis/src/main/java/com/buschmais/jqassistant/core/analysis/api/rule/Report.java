package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Properties;

/**
 * Report definition for a rule.
 */
public class Report {

    public static final String DEFAULT_TYPE = "default";

    private String type;

    private String primaryColumn;

    private Properties properties;

    public Report(String type, String primaryColumn, Properties properties) {
        this.type = type;
        this.primaryColumn = primaryColumn;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public String getPrimaryColumn() {
        return primaryColumn;
    }

    public Properties getProperties() {
        return properties;
    }
}
