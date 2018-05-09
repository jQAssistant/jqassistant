package com.buschmais.jqassistant.commandline.test;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.commandline.task.ReportTask;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Verifies command line reporting.
 */
public class ReportIT extends com.buschmais.jqassistant.commandline.test.AbstractCLIIT {

    public ReportIT(String neo4jVersion) {
        super(neo4jVersion);
    }

    @Test
    public void report() throws IOException, InterruptedException {
        String rulesDirectory = ReportIT.class.getResource("/rules").getFile();
        String[] args1 = new String[] { "analyze", "-r", rulesDirectory };
        assertThat(execute(args1).getExitCode(), equalTo(2));
        assertThat(new File(getDefaultReportDirectory(), Task.REPORT_FILE_XML).exists(), equalTo(true));
        String[] args2 = new String[] { "report" };
        assertThat(execute(args2).getExitCode(), equalTo(0));
        assertThat(new File(getDefaultReportDirectory(), ReportTask.REPORT_FILE_HTML).exists(), equalTo(true));
    }

}
