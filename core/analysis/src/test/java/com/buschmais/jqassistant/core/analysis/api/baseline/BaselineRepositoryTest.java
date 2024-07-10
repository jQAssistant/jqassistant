package com.buschmais.jqassistant.core.analysis.api.baseline;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.of;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class BaselineRepositoryTest {

    private static final File RULE_DIRECTORY = new File("target/test-classes");
    private static final File BASELINE_FILE = new File(RULE_DIRECTORY, "jqassistant-baseline.xml");
    public static final File UPDATED_BASELINE_FILE = new File(RULE_DIRECTORY, "jqassistant-baseline.updated.xml");

    @Mock
    private com.buschmais.jqassistant.core.analysis.api.configuration.Baseline configuration;

    @Test
    void readNonExisting() {
        doReturn(of("non-existing-baseline.xml")).when(configuration)
            .file();
        BaselineRepository baselineRepository = new BaselineRepository(configuration, RULE_DIRECTORY);

        assertThat(baselineRepository.read()).isNotPresent();
    }

    @Test
    void readFromConfiguredFile() {
        doReturn(of(BASELINE_FILE.getAbsolutePath())).when(configuration)
            .file();

        BaselineRepository baselineRepository = new BaselineRepository(configuration, new File("."));

        Optional<Baseline> optionalBaseline = baselineRepository.read();
        verify(optionalBaseline);
    }

    @Test
    void readFromRuleDirectory() {
        BaselineRepository baselineRepository = new BaselineRepository(configuration, RULE_DIRECTORY);

        Optional<Baseline> optionalBaseline = baselineRepository.read();
        verify(optionalBaseline);
    }

    @Test
    void update() throws IOException {
        FileUtils.copyFile(BASELINE_FILE, UPDATED_BASELINE_FILE);
        doReturn(of(UPDATED_BASELINE_FILE.getAbsolutePath())).when(configuration)
            .file();
        BaselineRepository baselineRepository = new BaselineRepository(configuration, new File("."));

        Optional<Baseline> optionalBaseline = baselineRepository.read();
        assertThat(optionalBaseline).isPresent();
        baselineRepository.write(optionalBaseline.get());

        assertThat(readFileToString(UPDATED_BASELINE_FILE, UTF_8)).isEqualTo(readFileToString(BASELINE_FILE, UTF_8));
    }

    private static void verify(Optional<Baseline> optionalBaseline) {
        assertThat(optionalBaseline).isPresent();
        Baseline baseline = optionalBaseline.get();
        verify(baseline.getConcepts(), "test-concept");
        verify(baseline.getConstraints(), "test-constraint");
    }

    private static void verify(SortedMap<String, Baseline.RuleBaseline> ruleBaselines, String ruleId) {
        assertThat(ruleBaselines).hasSize(1);
        Baseline.RuleBaseline ruleBaseline = ruleBaselines.get(ruleId);
        assertThat(ruleBaseline).isNotNull();
        SortedMap<String, SortedMap<String, String>> rows = ruleBaseline.getRows();
        assertThat(rows).hasSize(1);
        SortedMap<String, String> row = rows.get("1");
        assertThat(row).isNotNull()
            .hasSize(2)
            .containsEntry("c1", "value 1")
            .containsEntry("c2", "value 2");
    }

}
