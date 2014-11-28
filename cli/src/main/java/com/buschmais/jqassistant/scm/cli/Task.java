package com.buschmais.jqassistant.scm.cli;

import com.buschmais.jqassistant.scm.cli.task.AnalyzeTask;
import com.buschmais.jqassistant.scm.cli.task.AvailableRulesTask;
import com.buschmais.jqassistant.scm.cli.task.AvailableScopesTask;
import com.buschmais.jqassistant.scm.cli.task.EffectiveRulesTask;
import com.buschmais.jqassistant.scm.cli.task.ReportTask;
import com.buschmais.jqassistant.scm.cli.task.ResetTask;
import com.buschmais.jqassistant.scm.cli.task.ScanTask;
import com.buschmais.jqassistant.scm.cli.task.ServerTask;
import com.google.common.base.CaseFormat;

/**
 * Define all known tasks.
 */
public enum Task {
    /**
     * Scan.
     */
    SCAN {
        @Override
        public JQATask getTask() {
            return new ScanTask();
        }
    },
    /**
     * Available scopes.
     */
    AVAILABLE_SCOPES {
        @Override
        public JQATask getTask() {
            return new AvailableScopesTask();
        }
    },
    /**
     * Server.
     */
    SERVER {
        @Override
        public JQATask getTask() {
            return new ServerTask();
        }
    },
    /**
     * Available rules.
     */
    AVAILABLE_RULES {
        @Override
        public JQATask getTask() {
            return new AvailableRulesTask();
        }
    },
    /**
     * Available rules.
     */
    EFFECTIVE_RULES {
        @Override
        public JQATask getTask() {
            return new EffectiveRulesTask();
        }
    },
    /**
     * Analyze.
     */
    ANALYZE {
        @Override
        public JQATask getTask() {
            return new AnalyzeTask();
        }
    },
    /**
     * Reset.
     */
    RESET {
        @Override
        public JQATask getTask() {
            return new ResetTask();
        }
    },
    /**
     * Report.
     */
    REPORT {
        @Override
        public JQATask getTask() {
            return new ReportTask();
        }
    };

    /**
     * Return the task instance.
     * 
     * @return The task instance.
     */
    public abstract JQATask getTask();

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
