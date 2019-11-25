package com.buschmais.jqassistant.core.rule.api.model;

import java.util.*;

import lombok.*;

/**
 * Report definition for a rule.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Report {

    private Set<String> selectedTypes = null;

    private String primaryColumn = null;

    @Builder.Default
    private Properties properties = new Properties();

    public static Set<String> selectTypes(String reportTypes) {
        Set<String> selectedTypes = new TreeSet<>();
        Scanner scanner = new Scanner(reportTypes).useDelimiter(",");
        while (scanner.hasNext()) {
            String reportType = scanner.next();
            selectedTypes.add(reportType.trim());
        }
        return selectedTypes;
    }

}
