package com.buschmais.jqassistant.scm.cli.task;

import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.cli.Task;
import com.buschmais.jqassistant.scm.cli.TaskFactory;

import com.google.common.base.CaseFormat;

/**
 * Default implementation of a task factory.
 */
public class DefaultTaskFactoryImpl implements TaskFactory {

    public Task fromName(String name) throws CliExecutionException {
        String formattedTaskName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, name);
        try {
            return DefaultTask.valueOf(formattedTaskName).getTask();
        } catch (IllegalArgumentException e) {
            throw new CliExecutionException("Cannot determine task for " + name);
        }
    }

    @Override
    public List<Task> getTasks() {
        List<Task> taskNames = new ArrayList<>();
        for (DefaultTask defaultTask : DefaultTask.values()) {
            taskNames.add(defaultTask.getTask());
        }
        return taskNames;
    }

    @Override
    public List<String> getTaskNames() {
        List<String> taskNames = new ArrayList<>();
        for (DefaultTask defaultTask : DefaultTask.values()) {
            taskNames.add(defaultTask.name().toLowerCase());
        }
        return taskNames;
    }

    /**
     * Define all known tasks.
     */
    private enum DefaultTask {
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
         * Available rules.
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
        };

        /**
         * Return the task instance.
         *
         * @return The task instance.
         */
        public abstract Task getTask();

    }
}
