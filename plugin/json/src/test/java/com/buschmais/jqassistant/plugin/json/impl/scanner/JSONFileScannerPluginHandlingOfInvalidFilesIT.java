package com.buschmais.jqassistant.plugin.json.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.json.api.model.*;
import com.buschmais.jqassistant.plugin.json.parser.DataProvider;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;

@RunWith(Parameterized.class)
public class JSONFileScannerPluginHandlingOfInvalidFilesIT extends AbstractPluginIT {
    private File jsonFile;

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws Exception {
        return DataProvider.invalidFilesOfJsonParsingTestSuite();
    }

    @Before
    public void startTransaction() {
        store.beginTransaction();
    }

    @After
    public void commitTransaction() {
        store.commitTransaction();
    }

    public JSONFileScannerPluginHandlingOfInvalidFilesIT(File file) {
        jsonFile = file;
    }

    @Test
    public void canHandleInvalidJSONFile() throws Exception {
        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file.isValid(), equalTo(false));
    }
}
