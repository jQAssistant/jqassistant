package com.buschmais.jqassistant.scm.cli;

import com.buschmais.jqassistant.scm.cli.task.*;
import com.google.common.base.CaseFormat;

/**
 * Define all known tasks.
 */
public enum Task {
    /**
     * Scan.
     */
    SCAN(new ScanTask()),
    /**
     * Server.
     */
    SERVER(new ServerTask()),
    /**
     * Available rules.
     */
    AVAILABLE_RULES(new AvailableRulesTask()),
    /**
     * Available rules.
     */
    EFFECTIVE_RULES(new EffectiveRulesTask()),
    /**
     * Analyze.
     */
    ANALYZE(new AnalyzeTask()),
    /**
     * Reset.
     */
    RESET(new ResetTask()),
    /**
     * Report.
     */
    REPORT(new ReportTask());

    private JQATask task;

    /**
     * Constructor.
     * 
     * @param task
     */
    private Task(JQATask task) {
        this.task = task;
    }

    public JQATask getTask() {
        return task;
    }

    /**
     * Determine the task to execute from the given name.
     *
     * @param name
     *            The name.
     * @return The task.
     */
    public static JQATask fromName(String name) {
        String formattedTaskName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, name);
        try {
            return com.buschmais.jqassistant.scm.cli.Task.valueOf(formattedTaskName).getTask();
        } catch (IllegalArgumentException e) {
            System.exit(1);
        }
        return null;
    }

}
