package com.buschmais.jqassistant.core.rule.api.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;

/**
 * A rule source which is provided from a file.
 */
public class FileRuleSource extends RuleSource {

    private File file;

    public FileRuleSource(File file) {
        this.file = file;
    }

    @Override
    protected Type getType() {
        return selectTypeById();
    }

    @Override
    public String getId() {
        return file.getAbsolutePath();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public static List<RuleSource> getRuleSources(File rulesDirectory) throws IOException {
        final List<File> ruleFiles = new ArrayList<>();
        new DirectoryWalker<File>() {
            @Override
            protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
                boolean isFile = file.isFile();
                boolean isAsciiDocFile = Type.AsciiDoc.matches(file);
                boolean isXMLFile = Type.XML.matches(file);

                if (isFile && (isAsciiDocFile || isXMLFile)) {
                    results.add(file);
                }
            }

            public void scan(File directory) throws IOException {
                super.walk(directory, ruleFiles);
            }
        }.scan(rulesDirectory);

        List<RuleSource> ruleSources = new LinkedList<>();

        for (File ruleFile : ruleFiles) {
            ruleSources.add(new FileRuleSource(ruleFile));
        }

        return ruleSources;
    }

}
