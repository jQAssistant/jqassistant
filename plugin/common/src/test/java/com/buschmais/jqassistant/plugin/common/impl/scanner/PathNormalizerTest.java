package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class PathNormalizerTest {

    @TempDir
    private File projectDirectory;

    @Mock
    private ScannerContext scannerContext;

    @Test
    void relativizeChild() {
        File child = new File(projectDirectory, "src/main/java/Test.java");

        assertThat(PathNormalizer.relativize(projectDirectory, child)).isEqualTo("src/main/java/Test.java");
    }

    @Test
    void relativizeSameDirectory() {
        assertThat(PathNormalizer.relativize(projectDirectory, projectDirectory)).isEmpty();
    }

    @Test
    void relativizeNormalizesPaths() {
        File parent = new File(projectDirectory, "a/../b/.");
        File child = new File(projectDirectory, "b/./c/../d/Test.java");

        assertThat(PathNormalizer.relativize(parent, child)).isEqualTo("d/Test.java");
    }

    @Test
    void relativizeSibling() {
        File parent = new File(projectDirectory, "a");
        File child = new File(projectDirectory, "b/Test.java");

        assertThat(PathNormalizer.relativize(parent, child)).isEqualTo("../b/Test.java");
    }

    @Test
    void normalizePath() {
        doReturn(projectDirectory).when(scannerContext)
            .getProjectDirectory();
        File file = new File(projectDirectory, "target/classes/Test.class");

        assertThat(PathNormalizer.normalizePath(file, scannerContext)).isEqualTo("target/classes/Test.class");
    }

    @Test
    void normalizeFileName() {
        doReturn(projectDirectory).when(scannerContext)
            .getProjectDirectory();
        File file = new File(projectDirectory, "target/classes/Test.class");

        assertThat(PathNormalizer.normalizeFileName(file, scannerContext)).isEqualTo("/target/classes/Test.class");
    }

    @Test
    @EnabledOnOs(WINDOWS)
    void relativizeWindowsSeparators() {
        File child = new File(projectDirectory, "src\\main\\java\\Test.java");

        assertThat(PathNormalizer.relativize(projectDirectory, child)).isEqualTo("src/main/java/Test.java");
    }

    @Test
    @EnabledOnOs(WINDOWS)
    void relativizeWindowsDifferentDrives() {
        File parent = new File("C:\\project");
        File child = new File("D:\\other\\Test.java");

        assertThat(PathNormalizer.relativize(parent, child)).isEqualTo("D:/other/Test.java");
    }
}
