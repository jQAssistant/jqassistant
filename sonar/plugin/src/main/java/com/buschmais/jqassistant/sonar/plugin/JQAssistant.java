package com.buschmais.jqassistant.sonar.plugin;

/**
* Defines constants for the jQAssistant plugin.
*/
public final class JQAssistant {

    /**
     * Private constructor.
     */
    private JQAssistant() {
    }

    /**
     * The repository key.
     */
    public static final String KEY = "jqassistant";

    /**
     * The repository name.
     */
    public static final String NAME = "jQAssistant";

    /**
     * Maven properties key to define a default lookup folder for the jQAssistant report file.
     */
    public static final String SETTINGS_KEY_REPORT_PATH = "sonar.jqassistant.reportPath";

    /**
     * Filename of jQAssistant report file.
     */
    public static final String REPORT_FILE_NAME = "jqassistant/jqassistant-report.xml";

    /**
     * Maven properties key to deactivate the creation of issues for concepts that do not return a result.
     * Default is true.
     */
    public static final String SETTINGS_KEY_CREATE_EMPTY_CONCEPT_ISSUE = "sonar.jqassistant.createEmptyConceptIssue";

}