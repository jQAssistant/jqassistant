package com.buschmais.jqassistant.commandline.test;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.commandline.task.ReportTask;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line reporting.
 */
@ExtendWith(Neo4JTestTemplateInvocationContextProvider.class)
class ReportIT extends com.buschmais.jqassistant.commandline.test.AbstractCLIIT {

    @TestTemplate
    void report() throws IOException, InterruptedException {
        String rulesDirectory = ReportIT.class.getResource("/rules").getFile();
        String[] args1 = new String[] { "analyze", "report", "-r", rulesDirectory, "-concepts", "default:TestConcept" };
        assertThat(execute(args1).getExitCode()).isEqualTo(0);
        assertThat(new File(getDefaultReportDirectory(), ReportTask.REPORT_FILE_HTML).exists()).isTrue();
    }
}
