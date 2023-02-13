package com.buschmais.jqassistant.commandline.test;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.commandline.task.ReportTask;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line reporting.
 */
class ReportIT extends com.buschmais.jqassistant.commandline.test.AbstractCLIIT {

    @Test
    void report() throws IOException, InterruptedException {
        String[] args1 = new String[] { "analyze", "report", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.concepts=default:TestConcept" };
        assertThat(execute(args1).getExitCode()).isEqualTo(0);
        assertThat(new File(getDefaultReportDirectory(), ReportTask.REPORT_FILE_HTML).exists()).isTrue();
    }
}
