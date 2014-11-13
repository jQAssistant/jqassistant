package com.buschmais.jqassistant.scm.cli;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.scm.cli.task.AnalyzeTask;
import com.buschmais.jqassistant.scm.cli.task.AvailableRulesTask;
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
        public JQATask getTask(PluginConfigurationReader pluginConfigurationReader) {
            return new ScanTask(pluginConfigurationReader);
        }
    },
    /**
     * Server.
     */
    SERVER {
        @Override
        public JQATask getTask(PluginConfigurationReader pluginConfigurationReader) {
            return new ServerTask(pluginConfigurationReader);
        }
    },
    /**
     * Available rules.
     */
    AVAILABLE_RULES {
        @Override
        public JQATask getTask(PluginConfigurationReader pluginConfigurationReader) {
            return new AvailableRulesTask(pluginConfigurationReader);
        }
    },
    /**
     * Available rules.
     */
    EFFECTIVE_RULES {
        @Override
        public JQATask getTask(PluginConfigurationReader pluginConfigurationReader) {
            return new EffectiveRulesTask(pluginConfigurationReader);
        }
    },
    /**
     * Analyze.
     */
    ANALYZE {
        @Override
        public JQATask getTask(PluginConfigurationReader pluginConfigurationReader) {
            return new AnalyzeTask(pluginConfigurationReader);
        }
    },
    /**
     * Reset.
     */
    RESET {
        @Override
        public JQATask getTask(PluginConfigurationReader pluginConfigurationReader) {
            return new ResetTask(pluginConfigurationReader);
        }
    },
    /**
     * Report.
     */
    REPORT {
        @Override
        public JQATask getTask(PluginConfigurationReader pluginConfigurationReader) {
            return new ReportTask(pluginConfigurationReader);
        }
    };

    /**
     * Return the task instance.
     * 
     * @return The task instance.
     */
    public abstract JQATask getTask(PluginConfigurationReader pluginConfigurationReader);

    /**
     * Determine the task to execute from the given name.
     *
     * @param name
     *            The name.
     * @param pluginConfigurationReader
     *            The plugin configuration reader.
     * @return The task.
     */
    public static JQATask fromName(String name, PluginConfigurationReader pluginConfigurationReader) {
        String formattedTaskName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, name);
        try {
            return com.buschmais.jqassistant.scm.cli.Task.valueOf(formattedTaskName).getTask(pluginConfigurationReader);
        } catch (IllegalArgumentException e) {
            System.exit(1);
        }
        return null;
    }

}
