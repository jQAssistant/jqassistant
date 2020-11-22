package com.buschmais.jqassistant.core.report.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.rule.api.model.Concept;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportContextImplTest {

    @Test
    void archiveReports() throws IOException, ReportException {
        // given
        File reportDirectory = new File("target/report");
        ReportContextImpl reportContext = new ReportContextImpl(reportDirectory, reportDirectory);
        File file = new File(reportContext.getReportDirectory("test-plugin"), "test-report.txt");
        try (FileWriter fileWriter = new FileWriter(file)) {
            IOUtils.write("Test", fileWriter);
        }
        reportContext.addReport("test", Concept.builder().id("test:Concept").build(), ReportContext.ReportType.LINK, file.toURI().toURL());

        // when
        File reportArchive = reportContext.createReportArchive();

        // then
        assertThat(reportArchive).exists();
        List<String> reportEntries = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(reportArchive))) {
            ZipEntry nextEntry;
            while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                reportEntries.add(nextEntry.getName());
            }
        }
        assertThat(reportEntries).containsExactly("test-plugin/test-report.txt");
    }
}
