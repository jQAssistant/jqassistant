package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.File;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.impl.scanner.ClassFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.pojo.Pojo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.buschmais.jqassistant.core.scanner.api.DefaultScope.NONE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class JavaSourceFileIT extends AbstractJavaPluginIT {

    @ParameterizedTest
    @ValueSource(strings = { "", "src/test/java" })
    void sourceFile(String sourceDirectoryPath) {
        Map<String, Object> properties = sourceDirectoryPath.isEmpty() ? emptyMap() : Map.of(ClassFileScannerPlugin.PROPERTY_SOURCE_PATHS, sourceDirectoryPath);
        Scanner scanner = getScanner(properties);
        File testClassesDirectory = getClassesDirectory(Pojo.class);
        File testSourceDirectory = new File(testClassesDirectory, "../../src/test/java");
        scanner.scan(testSourceDirectory, null, NONE);

        execute(ARTIFACT_ID, (artifact, s) -> {
            scanner.scan(testSourceDirectory, null, NONE);
            return singletonList(scanner.scan(Pojo.class, Pojo.class.getName(), JavaScope.CLASSPATH));
        }, scanner);

        store.beginTransaction();
        TestResult testResult = query("MATCH (t:Java:ByteCode:Type:Class{fqn:$fqn}) RETURN t", Map.of("fqn", Pojo.class.getName()));
        assertThat(testResult.getRows()).hasSize(1);
        TypeClassFileDescriptor typeDescriptor = (TypeClassFileDescriptor) testResult.getRows()
            .get(0)
            .get("t");
        FileDescriptor sourceFileDescriptor = typeDescriptor.getHasSourceFile();
        assertThat(sourceFileDescriptor).isNotNull();
        assertThat(sourceFileDescriptor.getPath()).endsWith(Pojo.class.getSimpleName() + ".java");
        store.commitTransaction();
    }
}
