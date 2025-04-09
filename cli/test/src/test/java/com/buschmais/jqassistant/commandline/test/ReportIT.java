package com.buschmais.jqassistant.commandline.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.buschmais.jqassistant.commandline.task.ReportTask;
import com.buschmais.jqassistant.core.shared.xml.JAXBHelper;

import org.jqassistant.schema.report.v2.BuildPropertiesType;
import org.jqassistant.schema.report.v2.BuildProperty;
import org.jqassistant.schema.report.v2.BuildType;
import org.jqassistant.schema.report.v2.JqassistantReport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line reporting.
 */
class ReportIT extends com.buschmais.jqassistant.commandline.test.AbstractCLIIT {

    @DistributionTest
    void report() {
        String[] args1 = new String[] { "analyze", "report", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.concepts=default:TestConcept" };
        assertThat(execute(args1).getExitCode()).isZero();
        assertThat(new File(getDefaultReportDirectory(), ReportTask.REPORT_FILE_HTML)).exists();
    }

    @DistributionTest
    void buildProperty() throws IOException {
        String[] args1 = new String[] { "analyze", "report", "-D", "jqassistant.analyze.report.build.properties.Branch=develop" };
        assertThat(execute(args1).getExitCode()).isZero();

        File xmlReport = new File(getDefaultReportDirectory(), ReportTask.REPORT_FILE_XML);
        assertThat(xmlReport).exists();
        JAXBHelper<JqassistantReport> jaxbHelper = new JAXBHelper(JqassistantReport.class);
        BuildType buildType = jaxbHelper.unmarshal(new FileInputStream(xmlReport))
            .getBuild();
        assertThat(buildType).isNotNull();
        assertThat(buildType.getName()).endsWith(ReportIT.class.getSimpleName());
        BuildPropertiesType properties = buildType.getProperties();
        assertThat(properties).isNotNull();
        assertThat(properties.getProperty()).hasSize(1);
        BuildProperty buildProperty = properties.getProperty()
            .get(0);
        assertThat(buildProperty.getKey()).isEqualTo("Branch");
        assertThat(buildProperty.getValue()).isEqualTo("develop");
    }

}
