package com.buschmais.jqassistant.commandline.test;

import java.io.File;

import com.buschmais.jqassistant.commandline.task.ReportTask;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line reporting.
 */
class ReportIT extends com.buschmais.jqassistant.commandline.test.AbstractCLIIT {

    @DistributionTest
    void report()  {
        String[] args1 = new String[] { "analyze", "report", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.concepts=default:TestConcept" };
        assertThat(execute(args1).getExitCode()).isZero();
        assertThat(new File(getDefaultReportDirectory(), ReportTask.REPORT_FILE_HTML)).exists();
    }
}
