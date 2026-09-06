package com.buschmais.jqassistant.plugin.junit.test.scanner;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.junit.api.model.TestReportDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.model.TestSuiteDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestReportDirectoryScannerIT extends AbstractJavaPluginIT {

    /**
     * Verifies that test reports files are scanned.
     *
     */
    @Test
    public void reportFile() {
        store.beginTransaction();
        File classesDirectory = getClassesDirectory(TestReportDirectoryScannerIT.class);
        TestReportDirectoryDescriptor directory = getScanner().scan(classesDirectory, null, JunitScope.TESTREPORTS);
        assertThat(directory.getFileName()).isEqualTo("/target/test-classes");
        List<TestSuiteDescriptor> testSuiteDescriptors = query("MATCH (suite:TestSuite:File) RETURN suite").getColumn("suite");
        assertThat(testSuiteDescriptors).hasSize(2);
        store.commitTransaction();
    }

}
