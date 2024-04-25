package com.buschmais.jqassistant.commandline.task;

import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.Task;

import com.google.common.base.CaseFormat;

/**
 * Default implementation of a task factory.
 */
public enum RegisteredTask {

    /**
     * Scan.
     */
    SCAN {
        @Override
        public Task getTask() {
            return new ScanTask();
        }
    },

    /**
     * Available scopes.
     */
    AVAILABLE_SCOPES {
        @Override
        public Task getTask() {
            return new AvailableScopesTask();
        }
    },

    /**
     * Server.
     */
    SERVER {
        @Override
        public Task getTask() {
            return new ServerTask();
        }
    },

    /**
     * Available rules.
     */
    AVAILABLE_RULES {
        @Override
        public Task getTask() {
            return new AvailableRulesTask();
        }
    },

    /**
     * Effective configuration.
     */
    EFFECTIVE_CONFIGURATION {
        @Override
        public Task getTask() {
            return new EffectiveConfigurationTask();
        }
    },

    /**
     * Effective rules.
     */
    EFFECTIVE_RULES {
        @Override
        public Task getTask() {
            return new EffectiveRulesTask();
        }
    },

    /**
     * Analyze.
     */
    ANALYZE {
        @Override
        public Task getTask() {
            return new AnalyzeTask();
        }
    },

    /**
     * Reset.
     */
    RESET {
        @Override
        public Task getTask() {
            return new ResetTask();
        }
    },

    /**
     * Report.
     */
    REPORT {
        @Override
        public Task getTask() {
            return new ReportTask();
        }
    },

    /**
     * Help
     */
    HELP {
        public Task getTask() {
            return new HelpTask();
        }
    };

    /**
     * Return the task instance.
     *
     * @return The task instance.
     */
    public abstract Task getTask();

    public static Task fromName(String name) throws CliExecutionException {
        String formattedTaskName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, name);
        try {
            return valueOf(formattedTaskName).getTask();
        } catch (IllegalArgumentException e) {
            throw new CliExecutionException("Cannot determine task for " + name);
        }
    }

    public static List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        for (RegisteredTask registeredTask : values()) {
            tasks.add(registeredTask.getTask());
        }
        return tasks;
    }

    public static List<String> getTaskNames() {
        List<String> taskNames = new ArrayList<>();
        for (RegisteredTask registeredTask : values()) {
            taskNames.add(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, registeredTask.name()));
        }
        return taskNames;
    }

}
