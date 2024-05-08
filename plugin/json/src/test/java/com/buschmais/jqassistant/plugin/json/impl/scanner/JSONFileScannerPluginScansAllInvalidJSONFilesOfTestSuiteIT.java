package com.buschmais.jqassistant.plugin.json.impl.scanner;

import java.io.File;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.shared.io.FileNameNormalizer;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.json.api.model.JSONFileDescriptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class JSONFileScannerPluginScansAllInvalidJSONFilesOfTestSuiteIT extends AbstractPluginIT {

    @BeforeEach
    void startTransaction() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTransaction() {
        if (store.hasActiveTransaction()) {
            store.commitTransaction();
        }
    }

    static Stream<File> data() throws URISyntaxException {
        return DataProvider.invalidFilesOfJSONParsingTestSuite();
    }

    @ParameterizedTest
    @MethodSource("data")
    void scannerScansAValidFileOfTheTestSuite(File pathToJSONFile) {
        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(pathToJSONFile, pathToJSONFile.getAbsolutePath(), null);

        assertThat(file).describedAs("Scanner must be able to return a descriptor.")
                        .isNotNull();
        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith(FileNameNormalizer.normalize(pathToJSONFile));
        assertThat(file.isValid()).isFalse();
    }
}
