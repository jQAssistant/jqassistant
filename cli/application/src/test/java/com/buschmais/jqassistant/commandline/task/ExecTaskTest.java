package com.buschmais.jqassistant.commandline.task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.runtime.api.configuration.Exec;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;
import com.buschmais.xo.api.ResultIterator;

import org.apache.commons.cli.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExecTask.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExecTaskTest extends AbstractTaskTest {

    private ExecTask execTask;

    @Mock
    private Exec exec;

    @Mock
    private Store store;

    @Mock
    private Query.Result<CompositeRowObject> queryResult;

    @Mock
    private CompositeRowObject row1;

    @Mock
    private CompositeRowObject row2;

    @TempDir
    File tempDir;

    private ByteArrayOutputStream outputCapture;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        execTask = new ExecTask();
        execTask.initialize(pluginRepository, storeFactory);

        // Capture System.out for output verification
        outputCapture = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputCapture));
    }

    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void optionsIncludeQueryAndQueryFile() {
        assertThat(execTask.getOptions()).hasSize(2);
        assertThat(execTask.getOptions()).anyMatch(opt -> opt.getOpt().equals("q") && opt.getLongOpt().equals("query"));
        assertThat(execTask.getOptions()).anyMatch(opt -> opt.getOpt().equals("qf") && opt.getLongOpt().equals("query-file"));
    }

    @Test
    void configureWithQuery() throws ParseException, CliConfigurationException {
        Options options = new Options();
        for (Option option : execTask.getOptions()) {
            options.addOption(option);
        }
        CommandLineParser parser = new BasicParser();
        CommandLine commandLine = parser.parse(options, new String[] { "-q", "MATCH (n) RETURN n" });

        ConfigurationBuilder configurationBuilder = mock(ConfigurationBuilder.class);
        when(configurationBuilder.with(any(Class.class), anyString(), anyString())).thenReturn(configurationBuilder);
        when(configurationBuilder.with(any(Class.class), anyString(), anyBoolean())).thenReturn(configurationBuilder);

        execTask.configure(commandLine, configurationBuilder);

        verify(configurationBuilder).with(eq(Exec.class), eq(Exec.QUERY), eq("MATCH (n) RETURN n"));
    }

    @Test
    void configureWithQueryFile() throws ParseException, CliConfigurationException {
        Options options = new Options();
        for (Option option : execTask.getOptions()) {
            options.addOption(option);
        }
        CommandLineParser parser = new BasicParser();
        CommandLine commandLine = parser.parse(options, new String[] { "-qf", "/path/to/query.cypher" });

        ConfigurationBuilder configurationBuilder = mock(ConfigurationBuilder.class);
        when(configurationBuilder.with(any(Class.class), anyString(), anyString())).thenReturn(configurationBuilder);
        when(configurationBuilder.with(any(Class.class), anyString(), anyBoolean())).thenReturn(configurationBuilder);

        execTask.configure(commandLine, configurationBuilder);

        verify(configurationBuilder).with(eq(Exec.class), eq(Exec.QUERY_FILE), eq("/path/to/query.cypher"));
    }

    @Test
    void connectorNotRequired() {
        assertThat(execTask.isConnectorRequired()).isFalse();
    }

    @Test
    void runWithQueryExecutesAndCommits() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("RETURN 1 as value"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("table");
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithResults();

        // Execute
        execTask.run(configuration, new Options());

        // Verify store interactions
        verify(store).beginTransaction();
        verify(store).executeQuery("RETURN 1 as value");
        verify(store).commitTransaction();
        verify(store, never()).rollbackTransaction();
    }

    @Test
    void runWithReadOnlyRollsBack() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("CREATE (n:Test) RETURN n"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(true);
        when(exec.outputFormat()).thenReturn("table");
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithResults();

        // Execute
        execTask.run(configuration, new Options());

        // Verify rollback was called
        verify(store).beginTransaction();
        verify(store).rollbackTransaction();
        verify(store, never()).commitTransaction();
    }

    @Test
    void runWithQueryFileReadsFile() throws Exception {
        // Create a query file
        File queryFile = new File(tempDir, "query.cypher");
        Files.writeString(queryFile.toPath(), "MATCH (n) RETURN n.name as name");

        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.empty());
        when(exec.queryFile()).thenReturn(Optional.of(queryFile.getAbsolutePath()));
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("table");
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithResults();

        // Execute
        execTask.run(configuration, new Options());

        // Verify query from file was executed
        verify(store).executeQuery("MATCH (n) RETURN n.name as name");
    }

    @Test
    void runWithMissingQueryFileThrowsException() {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.empty());
        when(exec.queryFile()).thenReturn(Optional.of("/nonexistent/file.cypher"));

        // Execute and verify exception
        assertThatThrownBy(() -> execTask.run(configuration, new Options()))
            .isInstanceOf(CliExecutionException.class)
            .hasMessageContaining("Query file not found");
    }

    @Test
    void runWithoutQueryOrFileThrowsException() {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.empty());
        when(exec.queryFile()).thenReturn(Optional.empty());

        // Execute and verify exception
        assertThatThrownBy(() -> execTask.run(configuration, new Options()))
            .isInstanceOf(CliExecutionException.class)
            .hasMessageContaining("Either query (-q) or query-file (-qf) must be specified");
    }

    @Test
    void runWithTableFormatOutputsTable() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("RETURN 1 as col1, 'test' as col2"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("table");
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithTwoColumnResults();

        // Execute
        execTask.run(configuration, new Options());

        // Verify table output
        String output = outputCapture.toString();
        assertThat(output).contains("col1");
        assertThat(output).contains("col2");
        assertThat(output).contains("row(s)");
    }

    @Test
    void runWithJsonFormatOutputsJson() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("RETURN 1 as value"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("json");
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithResults();

        // Execute
        execTask.run(configuration, new Options());

        // Verify JSON output
        String output = outputCapture.toString();
        assertThat(output).contains("[");
        assertThat(output).contains("]");
        assertThat(output).contains("\"value\"");
    }

    @Test
    void runWithCsvFormatOutputsCsv() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("RETURN 1 as col1, 2 as col2"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("csv");
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithTwoColumnResults();

        // Execute
        execTask.run(configuration, new Options());

        // Verify CSV output
        String output = outputCapture.toString();
        assertThat(output).contains("col1,col2");
    }

    @Test
    void runWithOutputFileWritesToFile() throws Exception {
        File outputFile = new File(tempDir, "output.txt");

        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("RETURN 1 as value"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("table");
        when(exec.outputFile()).thenReturn(Optional.of(outputFile.getAbsolutePath()));

        setupMockStoreWithResults();

        // Execute
        execTask.run(configuration, new Options());

        // Verify file was created and contains output
        assertThat(outputFile).exists();
        String content = Files.readString(outputFile.toPath());
        assertThat(content).contains("value");
    }

    @Test
    void runWithUnsupportedFormatThrowsException() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("RETURN 1"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("xml"); // unsupported format
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithResults();

        // Execute and verify exception
        assertThatThrownBy(() -> execTask.run(configuration, new Options()))
            .isInstanceOf(CliExecutionException.class)
            .hasMessageContaining("Unsupported output format");
    }

    @Test
    void runWithEmptyResultsOutputsNoResults() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("MATCH (n:NonExistent) RETURN n"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("table");
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithEmptyResults();

        // Execute
        execTask.run(configuration, new Options());

        // Verify empty results message
        String output = outputCapture.toString();
        assertThat(output).contains("(no results)");
    }

    @Test
    void runWithQueryExecutionErrorRollsBack() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("INVALID CYPHER"));
        when(exec.queryFile()).thenReturn(Optional.empty());

        setupMockStoreWithError();

        // Execute and verify exception
        assertThatThrownBy(() -> execTask.run(configuration, new Options()))
            .isInstanceOf(CliExecutionException.class)
            .hasMessageContaining("Failed to execute query");

        // Verify rollback was called
        verify(store).rollbackTransaction();
    }

    @Test
    void runWithNullValuesFormatsCorrectly() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("RETURN null as value"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("table");
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithNullValue();

        // Execute
        execTask.run(configuration, new Options());

        // Verify null is formatted
        String output = outputCapture.toString();
        assertThat(output).contains("null");
    }

    @Test
    void runWithJsonOutputHandlesSpecialCharacters() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("RETURN 'test' as value"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("json");
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithSpecialCharacters();

        // Execute
        execTask.run(configuration, new Options());

        // Verify JSON escaping
        String output = outputCapture.toString();
        assertThat(output).contains("\\n"); // escaped newline
    }

    @Test
    void runWithCsvOutputHandlesCommas() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("RETURN 'a,b' as value"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("csv");
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithCommaValue();

        // Execute
        execTask.run(configuration, new Options());

        // Verify CSV escaping (value with comma should be quoted)
        String output = outputCapture.toString();
        assertThat(output).contains("\"a,b\"");
    }

    @Test
    void runWithNumericValueInJsonOutputsWithoutQuotes() throws Exception {
        // Setup mocks
        when(configuration.exec()).thenReturn(exec);
        when(exec.query()).thenReturn(Optional.of("RETURN 42 as value"));
        when(exec.queryFile()).thenReturn(Optional.empty());
        when(exec.readOnly()).thenReturn(false);
        when(exec.outputFormat()).thenReturn("json");
        when(exec.outputFile()).thenReturn(Optional.empty());

        setupMockStoreWithNumericValue();

        // Execute
        execTask.run(configuration, new Options());

        // Verify numeric value without quotes
        String output = outputCapture.toString();
        assertThat(output).contains("\"value\": 42");
    }

    // Helper methods to setup mock store

    private void setupMockStoreWithResults() throws Exception {
        when(storeFactory.getStore(any(), any())).thenReturn(store);
        when(store.executeQuery(anyString())).thenReturn(queryResult);

        when(row1.getColumns()).thenReturn(Arrays.asList("value"));
        when(row1.get("value", Object.class)).thenReturn(1);

        doReturn(asResultIterator(Arrays.asList(row1))).when(queryResult).iterator();
    }

    private void setupMockStoreWithTwoColumnResults() throws Exception {
        when(storeFactory.getStore(any(), any())).thenReturn(store);
        when(store.executeQuery(anyString())).thenReturn(queryResult);

        when(row1.getColumns()).thenReturn(Arrays.asList("col1", "col2"));
        when(row1.get("col1", Object.class)).thenReturn(1);
        when(row1.get("col2", Object.class)).thenReturn("test");

        doReturn(asResultIterator(Arrays.asList(row1))).when(queryResult).iterator();
    }

    private void setupMockStoreWithEmptyResults() throws Exception {
        when(storeFactory.getStore(any(), any())).thenReturn(store);
        when(store.executeQuery(anyString())).thenReturn(queryResult);

        doReturn(asResultIterator(Arrays.<CompositeRowObject>asList())).when(queryResult).iterator();
    }

    private void setupMockStoreWithError() throws Exception {
        when(storeFactory.getStore(any(), any())).thenReturn(store);
        when(store.executeQuery(anyString())).thenThrow(new RuntimeException("Syntax error"));
    }

    private void setupMockStoreWithNullValue() throws Exception {
        when(storeFactory.getStore(any(), any())).thenReturn(store);
        when(store.executeQuery(anyString())).thenReturn(queryResult);

        when(row1.getColumns()).thenReturn(Arrays.asList("value"));
        when(row1.get("value", Object.class)).thenReturn(null);

        doReturn(asResultIterator(Arrays.asList(row1))).when(queryResult).iterator();
    }

    private void setupMockStoreWithSpecialCharacters() throws Exception {
        when(storeFactory.getStore(any(), any())).thenReturn(store);
        when(store.executeQuery(anyString())).thenReturn(queryResult);

        when(row1.getColumns()).thenReturn(Arrays.asList("value"));
        when(row1.get("value", Object.class)).thenReturn("line1\nline2");

        doReturn(asResultIterator(Arrays.asList(row1))).when(queryResult).iterator();
    }

    private void setupMockStoreWithCommaValue() throws Exception {
        when(storeFactory.getStore(any(), any())).thenReturn(store);
        when(store.executeQuery(anyString())).thenReturn(queryResult);

        when(row1.getColumns()).thenReturn(Arrays.asList("value"));
        when(row1.get("value", Object.class)).thenReturn("a,b");

        doReturn(asResultIterator(Arrays.asList(row1))).when(queryResult).iterator();
    }

    private void setupMockStoreWithNumericValue() throws Exception {
        when(storeFactory.getStore(any(), any())).thenReturn(store);
        when(store.executeQuery(anyString())).thenReturn(queryResult);

        when(row1.getColumns()).thenReturn(Arrays.asList("value"));
        when(row1.get("value", Object.class)).thenReturn(42);

        doReturn(asResultIterator(Arrays.asList(row1))).when(queryResult).iterator();
    }

    /**
     * Helper to create a ResultIterator from a list.
     */
    private static ResultIterator<CompositeRowObject> asResultIterator(List<CompositeRowObject> rows) {
        java.util.Iterator<CompositeRowObject> iterator = rows.iterator();
        return new ResultIterator<CompositeRowObject>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public CompositeRowObject next() {
                return iterator.next();
            }

            @Override
            public void close() {
            }
        };
    }
}
