package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.asciidoctor.extension.ExtensionGroup;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.RubyExtensionRegistry;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterRegistry;

/**
 * A delegate the replaces {@link #javaExtensionRegistry()} to fix classloading
 * issues for registered extensions if another classloader is used.
 */
public class AsciidoctorDelegate implements Asciidoctor {

    private final Asciidoctor delegate;

    public AsciidoctorDelegate(Asciidoctor delegate) {
        this.delegate = delegate;
    }

    @Override
    public String convert(String content, Map<String, Object> options) {
        return delegate.convert(content, options);
    }

    @Override
    public <T> T convert(String s, Map<String, Object> map, Class<T> aClass) {
        return delegate.convert(s, map, aClass);
    }

    @Override
    public String convert(String content, Options options) {
        return delegate.convert(content, options);
    }

    @Override
    public <T> T convert(String s, Options options, Class<T> aClass) {
        return delegate.convert(s, options, aClass);
    }

    @Override
    public String convert(String content, OptionsBuilder options) {
        return delegate.convert(content, options);
    }

    @Override
    public <T> T convert(String s, OptionsBuilder optionsBuilder, Class<T> aClass) {
        return delegate.convert(s, optionsBuilder, aClass);
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, Map<String, Object> options) throws IOException {
        delegate.convert(contentReader, rendererWriter, options);
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, Options options) throws IOException {
        delegate.convert(contentReader, rendererWriter, options);
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, OptionsBuilder options) throws IOException {
        delegate.convert(contentReader, rendererWriter, options);
    }

    @Override
    public String convertFile(File filename, Map<String, Object> options) {
        return delegate.convertFile(filename, options);
    }

    @Override
    public <T> T convertFile(File file, Map<String, Object> map, Class<T> aClass) {
        return delegate.convertFile(file, map, aClass);
    }

    @Override
    public String convertFile(File filename, Options options) {
        return delegate.convertFile(filename, options);
    }

    @Override
    public <T> T convertFile(File file, Options options, Class<T> aClass) {
        return delegate.convertFile(file, options, aClass);
    }

    @Override
    public String convertFile(File filename, OptionsBuilder options) {
        return delegate.convertFile(filename, options);
    }

    @Override
    public <T> T convertFile(File file, OptionsBuilder optionsBuilder, Class<T> aClass) {
        return delegate.convertFile(file, optionsBuilder, aClass);
    }

    @Override
    public String[] convertDirectory(Iterable<File> iterable, Map<String, Object> map) {
        return delegate.convertDirectory(iterable, map);
    }

    @Override
    public String[] convertDirectory(Iterable<File> iterable, Options options) {
        return delegate.convertDirectory(iterable, options);
    }

    @Override
    public String[] convertDirectory(Iterable<File> iterable, OptionsBuilder optionsBuilder) {
        return delegate.convertDirectory(iterable, optionsBuilder);
    }

    @Override
    public String[] convertFiles(Collection<File> asciidoctorFiles, Map<String, Object> options) {
        return delegate.convertFiles(asciidoctorFiles, options);
    }

    @Override
    public String[] convertFiles(Collection<File> asciidoctorFiles, Options options) {
        return delegate.convertFiles(asciidoctorFiles, options);
    }

    @Override
    public String[] convertFiles(Collection<File> asciidoctorFiles, OptionsBuilder options) {
        return delegate.convertFiles(asciidoctorFiles, options);
    }

    @Override
    public DocumentHeader readDocumentHeader(File filename) {
        return delegate.readDocumentHeader(filename);
    }

    @Override
    public DocumentHeader readDocumentHeader(String content) {
        return delegate.readDocumentHeader(content);
    }

    @Override
    public DocumentHeader readDocumentHeader(Reader contentReader) {
        return delegate.readDocumentHeader(contentReader);
    }

    @Override
    public void requireLibrary(String... requiredLibraries) {
        delegate.requireLibrary(requiredLibraries);
    }

    @Override
    public void requireLibraries(Collection<String> requiredLibraries) {
        delegate.requireLibraries(requiredLibraries);
    }

    @Override
    public JavaExtensionRegistry javaExtensionRegistry() {
        return new JavaExtensionRegistryDelegate(delegate.javaExtensionRegistry());
    }

    @Override
    public RubyExtensionRegistry rubyExtensionRegistry() {
        return delegate.rubyExtensionRegistry();
    }

    @Override
    public JavaConverterRegistry javaConverterRegistry() {
        return delegate.javaConverterRegistry();
    }

    @Override
    public SyntaxHighlighterRegistry syntaxHighlighterRegistry() {
        return new SyntaxHighlighterRegistryDelegate(delegate.syntaxHighlighterRegistry());
    }

    @Override
    public ExtensionGroup createGroup() {
        return delegate.createGroup();
    }

    @Override
    public ExtensionGroup createGroup(String groupName) {
        return delegate.createGroup(groupName);
    }

    @Override
    public void unregisterAllExtensions() {
        delegate.unregisterAllExtensions();
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public String asciidoctorVersion() {
        return delegate.asciidoctorVersion();
    }

    @Override
    public Document load(String content, Map<String, Object> options) {
        return delegate.load(content, options);
    }

    @Override
    public Document load(String s, Options options) {
        return delegate.load(s, options);
    }

    @Override
    public Document loadFile(File file, Map<String, Object> options) {
        return delegate.loadFile(file, options);
    }

    @Override
    public Document loadFile(File file, Options options) {
        return delegate.loadFile(file, options);
    }

    @Override
    public void registerLogHandler(LogHandler logHandler) {
        delegate.registerLogHandler(logHandler);
    }

    @Override
    public void unregisterLogHandler(LogHandler logHandler) {
        delegate.unregisterLogHandler(logHandler);
    }
}
