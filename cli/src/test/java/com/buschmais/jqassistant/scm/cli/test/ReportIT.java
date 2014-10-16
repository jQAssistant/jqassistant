package com.buschmais.jqassistant.scm.cli.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.scm.cli.JQATask;
import com.buschmais.jqassistant.scm.cli.ReportTask;

/**
 * Verifies command line reporting.
 */
public class ReportIT extends AbstractCLIIT {

    @Test
    public void report() throws IOException, InterruptedException {
        String rulesDirectory = ReportIT.class.getResource("/rules").getFile();
        String[] args1 = new String[] { "analyze", "-r", rulesDirectory };
        execute(args1);
        assertThat(new File(getDefaultReportDirectory(), JQATask.REPORT_FILE_XML).exists(), equalTo(true));
        String[] args2 = new String[] { "report" };
        execute(args2);
        assertThat(new File(getDefaultReportDirectory(), ReportTask.REPORT_FILE_HTML).exists(), equalTo(true));
    }

}
