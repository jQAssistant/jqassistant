package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.Exec;
import com.buschmais.jqassistant.core.shared.annotation.Description;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationBuilder;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task for executing a Cypher query against the store.
 */
@Description("Executes a Cypher query against the store.")
public class ExecTask extends AbstractStoreTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecTask.class);

    public static final String CMDLINE_OPTION_QUERY = "q";
    public static final String CMDLINE_LONG_OPTION_QUERY = "query";
    public static final String CMDLINE_OPTION_QUERY_FILE = "qf";
    public static final String CMDLINE_LONG_OPTION_QUERY_FILE = "query-file";

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        Exec exec = configuration.exec();
        String query = resolveQuery(exec);

        LOGGER.info("Executing query: {}", query);

        withStore(configuration, store -> {
            store.beginTransaction();
            try {
                Query.Result<CompositeRowObject> result = store.executeQuery(query);
                formatAndOutput(result, exec);
                if (exec.readOnly()) {
                    store.rollbackTransaction();
                    LOGGER.info("Transaction rolled back (read-only mode).");
                } else {
                    store.commitTransaction();
                }
            } catch (Exception e) {
                store.rollbackTransaction();
                throw new CliExecutionException("Failed to execute query: " + e.getMessage(), e);
            }
        });
    }

    private String resolveQuery(Exec exec) throws CliExecutionException {
        if (exec.query().isPresent()) {
            return exec.query().get();
        }
        if (exec.queryFile().isPresent()) {
            String queryFilePath = exec.queryFile().get();
            File queryFile = new File(queryFilePath);
            if (!queryFile.exists()) {
                throw new CliExecutionException("Query file not found: " + queryFilePath);
            }
            try {
                return Files.readString(queryFile.toPath(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new CliExecutionException("Failed to read query file: " + queryFilePath, e);
            }
        }
        throw new CliExecutionException("Either query (-q) or query-file (-qf) must be specified.");
    }

    private void formatAndOutput(Query.Result<CompositeRowObject> result, Exec exec) throws CliExecutionException {
        String format = exec.outputFormat().toLowerCase();
        try (PrintWriter writer = getOutputWriter(exec)) {
            switch (format) {
                case "table":
                    formatAsTable(result, writer);
                    break;
                case "json":
                    formatAsJson(result, writer);
                    break;
                case "csv":
                    formatAsCsv(result, writer);
                    break;
                default:
                    throw new CliExecutionException("Unsupported output format: " + format);
            }
        } catch (IOException e) {
            throw new CliExecutionException("Failed to write output: " + e.getMessage(), e);
        }
    }

    private PrintWriter getOutputWriter(Exec exec) throws IOException {
        if (exec.outputFile().isPresent()) {
            return new PrintWriter(new FileWriter(exec.outputFile().get(), StandardCharsets.UTF_8));
        }
        return new PrintWriter(System.out, true, StandardCharsets.UTF_8) {
            @Override
            public void close() {
                // Don't close System.out
                flush();
            }
        };
    }

    private void formatAsTable(Query.Result<CompositeRowObject> result, PrintWriter writer) {
        List<Map<String, Object>> rows = collectResults(result);
        if (rows.isEmpty()) {
            writer.println("(no results)");
            return;
        }

        List<String> columns = new ArrayList<>(rows.get(0).keySet());
        Map<String, Integer> columnWidths = calculateColumnWidths(rows, columns);

        // Print header
        StringBuilder header = new StringBuilder("|");
        StringBuilder separator = new StringBuilder("+");
        for (String column : columns) {
            int width = columnWidths.get(column);
            header.append(" ").append(padRight(column, width)).append(" |");
            separator.append("-".repeat(width + 2)).append("+");
        }
        writer.println(separator);
        writer.println(header);
        writer.println(separator);

        // Print rows
        for (Map<String, Object> row : rows) {
            StringBuilder line = new StringBuilder("|");
            for (String column : columns) {
                int width = columnWidths.get(column);
                Object value = row.get(column);
                line.append(" ").append(padRight(formatValue(value), width)).append(" |");
            }
            writer.println(line);
        }
        writer.println(separator);
        writer.println(rows.size() + " row(s)");
    }

    private void formatAsJson(Query.Result<CompositeRowObject> result, PrintWriter writer) {
        List<Map<String, Object>> rows = collectResults(result);
        writer.println("[");
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            writer.print("  {");
            int j = 0;
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (j > 0) {
                    writer.print(", ");
                }
                writer.print("\"" + escapeJson(entry.getKey()) + "\": " + toJsonValue(entry.getValue()));
                j++;
            }
            writer.print("}");
            if (i < rows.size() - 1) {
                writer.println(",");
            } else {
                writer.println();
            }
        }
        writer.println("]");
    }

    private void formatAsCsv(Query.Result<CompositeRowObject> result, PrintWriter writer) {
        List<Map<String, Object>> rows = collectResults(result);
        if (rows.isEmpty()) {
            return;
        }

        List<String> columns = new ArrayList<>(rows.get(0).keySet());

        // Print header
        writer.println(String.join(",", columns.stream().map(this::escapeCsv).collect(java.util.stream.Collectors.toList())));

        // Print rows
        for (Map<String, Object> row : rows) {
            List<String> values = new ArrayList<>();
            for (String column : columns) {
                values.add(escapeCsv(formatValue(row.get(column))));
            }
            writer.println(String.join(",", values));
        }
    }

    private List<Map<String, Object>> collectResults(Query.Result<CompositeRowObject> result) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (CompositeRowObject row : result) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (String column : row.getColumns()) {
                rowMap.put(column, row.get(column, Object.class));
            }
            rows.add(rowMap);
        }
        return rows;
    }

    private Map<String, Integer> calculateColumnWidths(List<Map<String, Object>> rows, List<String> columns) {
        Map<String, Integer> widths = new LinkedHashMap<>();
        for (String column : columns) {
            int maxWidth = column.length();
            for (Map<String, Object> row : rows) {
                String value = formatValue(row.get(column));
                maxWidth = Math.max(maxWidth, value.length());
            }
            widths.put(column, Math.min(maxWidth, 50)); // Cap at 50 chars
        }
        return widths;
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        return value.toString();
    }

    private String padRight(String s, int n) {
        if (s.length() >= n) {
            return s.substring(0, n);
        }
        return s + " ".repeat(n - s.length());
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeCsv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    @Override
    public void configure(CommandLine options, ConfigurationBuilder configurationBuilder) throws CliConfigurationException {
        super.configure(options, configurationBuilder);
        if (options.hasOption(CMDLINE_OPTION_QUERY)) {
            configurationBuilder.with(Exec.class, Exec.QUERY, options.getOptionValue(CMDLINE_OPTION_QUERY));
        }
        if (options.hasOption(CMDLINE_OPTION_QUERY_FILE)) {
            configurationBuilder.with(Exec.class, Exec.QUERY_FILE, options.getOptionValue(CMDLINE_OPTION_QUERY_FILE));
        }
    }

    @Override
    public void addTaskOptions(final List<Option> options) {
        super.addTaskOptions(options);
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_QUERY)
            .withLongOpt(CMDLINE_LONG_OPTION_QUERY)
            .withDescription("The Cypher query to execute.")
            .hasArg()
            .create(CMDLINE_OPTION_QUERY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_QUERY_FILE)
            .withLongOpt(CMDLINE_LONG_OPTION_QUERY_FILE)
            .withDescription("Path to a file containing the Cypher query to execute.")
            .hasArg()
            .create(CMDLINE_OPTION_QUERY_FILE));
    }
}
