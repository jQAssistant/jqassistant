package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Report definition for a rule.
 */
public class Report {

    private Set<String> selectedTypes = null;

    private String primaryColumn = null;

    private Properties properties = new Properties();

    private Report() {
    }

    public Set<String> getSelectedTypes() {
        return selectedTypes;
    }

    public String getPrimaryColumn() {
        return primaryColumn;
    }

    public Properties getProperties() {
        return properties;
    }

    public static class Builder {

        private Report report = new Report();

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder selectTypes(String reportTypes) {
            if (report.selectedTypes == null) {
                report.selectedTypes = new TreeSet<>();
            }
            Scanner scanner = new Scanner(reportTypes).useDelimiter(",");
            while (scanner.hasNext()) {
                String reportType = scanner.next();
                report.selectedTypes.add(reportType.trim());
            }
            return this;
        }

        public Builder property(String key, String value) {
            report.properties.setProperty(key, value);
            return this;
        }

        public Builder primaryColumn(String primaryColumn) {
            report.primaryColumn = primaryColumn;
            return this;
        }

        public Builder properties(Properties properties) {
            report.properties.putAll(properties);
            return this;
        }

        public Report get() {
            return report;
        }

    }
}
