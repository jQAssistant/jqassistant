package com.buschmais.jqassistant.core.report.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.store.api.Store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static lombok.AccessLevel.PRIVATE;

/**
 * Implementation of the {@link ReportContext}.
 */
public class ReportContextImpl implements ReportContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportContextImpl.class);

    private final ClassLoader classLoader;

    private final Store store;

    private final File outputDirectory;

    private final File reportDirectory;

    private final Map<String, List<Report<?>>> reports = new HashMap<>();

    /**
     * Constructor.
     *
     * @param store
     *            The {@link Store};
     * @param outputDirectory
     *            The output directory.
     */
    public ReportContextImpl(ClassLoader classLoader, Store store, File outputDirectory) {
        this(classLoader, store, outputDirectory, new File(outputDirectory, REPORT_DIRECTORY));
    }

    /**
     * Constructor.
     *
     * @param store
     *            The {@link Store};
     * @param outputDirectory
     *            The output directory.
     */
    public ReportContextImpl(ClassLoader classLoader, Store store, File outputDirectory, File reportDirectory) {
        this.classLoader = classLoader;
        this.store = store;
        this.outputDirectory = outputDirectory;
        this.reportDirectory = reportDirectory;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Store getStore() {
        return store;
    }

    @Override
    public File getReportDirectory(String path) {
        File directory = new File(reportDirectory, path);
        if (directory.mkdirs()) {
            LOGGER.debug("Created report directory '{}'.", directory.getAbsolutePath());
        }
        return directory;
    }

    @Override
    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public <E extends ExecutableRule<?>> Report<E> addReport(String label, E rule, ReportType reportType, URL url) {
        Report<E> report = ReportImpl.<E> builder().label(label).rule(rule).reportType(reportType).url(url).build();
        getReports(rule).add(report);
        return report;
    }

    @Override
    public <E extends ExecutableRule<?>> List<Report<?>> getReports(E rule) {
        List<Report<?>> ruleReports = reports.get(rule.getId());
        if (ruleReports == null) {
            ruleReports = new ArrayList<>();
            reports.put(rule.getId(), ruleReports);
        }
        return ruleReports;
    }

    @Override
    public File createReportArchive() throws ReportException {
        File reportArchive = new File(outputDirectory, JQASSISTANT_REPORT_ARCHIVE);
        if (reportArchive.exists() && !reportArchive.delete()) {
            throw new ReportException("Cannot delete existing report archive " + reportArchive.getAbsolutePath());
        }
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(reportArchive))) {
            Path reportPath = reportDirectory.toPath();
            Files.walkFileTree(reportPath, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.toFile().equals(reportArchive)) {
                        zos.putNextEntry(new ZipEntry(reportPath.relativize(file).toString().replace('\\', '/')));
                        Files.copy(file, zos);
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new ReportException("Cannot create report archive " + reportArchive.getAbsolutePath(), e);
        }
        return reportArchive;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    @ToString
    private static final class ReportImpl<E extends ExecutableRule<?>> implements Report<E> {

        private String label;

        private E rule;

        private ReportType reportType;

        private URL url;

    }
}
