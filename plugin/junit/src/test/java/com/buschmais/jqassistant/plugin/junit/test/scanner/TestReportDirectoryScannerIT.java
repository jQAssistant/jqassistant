package com.buschmais.jqassistant.plugin.junit.test.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.junit.api.model.TestReportDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.model.TestSuiteDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope;

public class TestReportDirectoryScannerIT extends AbstractJavaPluginIT {

    /**
     * Verifies that test reports files are scanned.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void reportFile() throws IOException {
        store.beginTransaction();
        File classesDirectory = getClassesDirectory(TestReportDirectoryScannerIT.class);
        String absolutePath = classesDirectory.getAbsolutePath();
        TestReportDirectoryDescriptor directory = getScanner().scan(classesDirectory, absolutePath, JunitScope.TESTREPORTS);
        assertThat(directory.getFileName(), equalTo(absolutePath.replace('\\', '/')));
        List<TestSuiteDescriptor> testSuiteDescriptors = query("MATCH (suite:TestSuite:File) RETURN suite").getColumn("suite");
        assertThat(testSuiteDescriptors.size(), equalTo(1));
        store.commitTransaction();
    }

}
