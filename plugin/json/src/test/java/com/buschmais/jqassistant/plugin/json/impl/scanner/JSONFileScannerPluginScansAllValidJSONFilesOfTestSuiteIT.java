package com.buschmais.jqassistant.plugin.json.impl.scanner;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.shared.io.FileNameNormalizer;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.json.api.model.JSONFileDescriptor;
import com.buschmais.jqassistant.plugin.json.impl.parsing.DataProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class JSONFileScannerPluginScansAllValidJSONFilesOfTestSuiteIT extends AbstractPluginIT {

    private final File pathToJSONFile;

    @Before
    public void startTransaction() {
        store.beginTransaction();
    }

    @After
    public void commitTransaction() {
        if (store.hasActiveTransaction()) {
            store.commitTransaction();
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws URISyntaxException {
        return DataProvider.validFilesOfJSONParsingTestSuite();
    }

    public JSONFileScannerPluginScansAllValidJSONFilesOfTestSuiteIT(File path) {
        pathToJSONFile = path;
    }

    @Test
    public void scannerScansAValidFileOfTheTestSuite() {
        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(pathToJSONFile, pathToJSONFile.getAbsolutePath(), null);

        assertThat(file).describedAs("Scanner must be able to scan the resource and to return a descriptor.")
                        .isNotNull();
        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith(FileNameNormalizer.normalize(pathToJSONFile));
        assertThat(file.isValid()).isTrue();
    }
}
