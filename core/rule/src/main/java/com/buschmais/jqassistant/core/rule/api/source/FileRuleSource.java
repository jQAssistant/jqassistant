package com.buschmais.jqassistant.core.rule.api.source;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import static java.util.Collections.emptyList;

/**
 * A rule source which is provided from a file.
 */
@Slf4j
public class FileRuleSource extends RuleSource {

    private final File directory;

    private final String relativePath;

    private final File file;

    public FileRuleSource(File directory, String relativePath) {
        this.directory = directory;
        this.relativePath = relativePath.replace('\\', '/');
        this.file = new File(directory, relativePath);
    }

    @Override
    public String getId() {
        return this.file.getAbsolutePath();
    }

    @Override
    public URL getURL() throws MalformedURLException {
        return this.file.getAbsoluteFile().toURI().toURL();
    }

    @Override
    public Optional<File> getDirectory() {
        return Optional.of(directory);
    }

    @Override
    public String getRelativePath() {
        return this.relativePath;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    public static List<RuleSource> getRuleSources(File rulesDirectory) throws IOException {
        if (!rulesDirectory.exists()) {
            log.debug("Rules directory {} does not exist, skipping.", rulesDirectory.getAbsolutePath());
            return emptyList();
        }
        log.info("Reading rules from directory {}.", rulesDirectory.getAbsolutePath());
        List<RuleSource> ruleSources = new LinkedList<>();
        Files.walkFileTree(rulesDirectory.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                String relativePath = rulesDirectory.toPath().relativize(path).toString();
                ruleSources.add(new FileRuleSource(rulesDirectory, relativePath));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }
        });
        return ruleSources;
    }

}
