package com.buschmais.jqassistant.commandline.test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the exec command for executing Cypher queries.
 */
class ExecIT extends AbstractCLIIT {

    @DistributionTest
    void execWithQuery() {
        // First scan something to populate the store
        ExecutionResult scanResult = execute("scan", "-f", RULES_DIRECTORY);
        assertThat(scanResult.getExitCode()).isZero();

        // Execute a simple query
        ExecutionResult execResult = execute("exec", "-q", "MATCH (n) RETURN count(n) as count");
        assertThat(execResult.getExitCode()).isZero();

        List<String> console = execResult.getStandardConsole();
        assertThat(console).anyMatch(line -> line.contains("count"));
        assertThat(console).anyMatch(line -> line.contains("row(s)"));
    }

    @DistributionTest
    void execWithReadOnly() {
        // First scan something to populate the store
        ExecutionResult scanResult = execute("scan", "-f", RULES_DIRECTORY);
        assertThat(scanResult.getExitCode()).isZero();

        // Execute a query that would create data, but in read-only mode
        ExecutionResult execResult = execute("exec", "-q", "CREATE (n:TestNode) RETURN n",
            "-Djqassistant.exec.read-only=true");
        assertThat(execResult.getExitCode()).isZero();

        List<String> errorConsole = execResult.getErrorConsole();
        assertThat(errorConsole).anyMatch(line -> line.contains("rolled back"));

        // Verify the node was not actually created
        ExecutionResult verifyResult = execute("exec", "-q", "MATCH (n:TestNode) RETURN count(n) as count");
        assertThat(verifyResult.getExitCode()).isZero();
        List<String> verifyConsole = verifyResult.getStandardConsole();
        assertThat(verifyConsole).anyMatch(line -> line.contains("0"));
    }

    @DistributionTest
    void execWithJsonOutput() {
        // First scan something to populate the store
        ExecutionResult scanResult = execute("scan", "-f", RULES_DIRECTORY);
        assertThat(scanResult.getExitCode()).isZero();

        // Execute with JSON output format
        ExecutionResult execResult = execute("exec", "-q", "RETURN 1 as value",
            "-Djqassistant.exec.output-format=json");
        assertThat(execResult.getExitCode()).isZero();

        List<String> console = execResult.getStandardConsole();
        assertThat(console).anyMatch(line -> line.contains("["));
        assertThat(console).anyMatch(line -> line.contains("\"value\""));
    }

    @DistributionTest
    void execWithCsvOutput() {
        // First scan something to populate the store
        ExecutionResult scanResult = execute("scan", "-f", RULES_DIRECTORY);
        assertThat(scanResult.getExitCode()).isZero();

        // Execute with CSV output format
        ExecutionResult execResult = execute("exec", "-q", "RETURN 1 as col1, 2 as col2",
            "-Djqassistant.exec.output-format=csv");
        assertThat(execResult.getExitCode()).isZero();

        List<String> console = execResult.getStandardConsole();
        assertThat(console).anyMatch(line -> line.equals("col1,col2"));
        assertThat(console).anyMatch(line -> line.equals("1,2"));
    }

    @DistributionTest
    void execWithQueryFile() throws Exception {
        // First scan something to populate the store
        ExecutionResult scanResult = execute("scan", "-f", RULES_DIRECTORY);
        assertThat(scanResult.getExitCode()).isZero();

        // Create a query file
        File queryFile = new File(getWorkingDirectory(), "query.cypher");
        java.nio.file.Files.writeString(queryFile.toPath(), "RETURN 'hello' as message");

        // Execute with query file
        ExecutionResult execResult = execute("exec", "-qf", queryFile.getAbsolutePath());
        assertThat(execResult.getExitCode()).isZero();

        List<String> console = execResult.getStandardConsole();
        assertThat(console).anyMatch(line -> line.contains("hello"));
    }

    @DistributionTest
    void execWithOutputFile() throws Exception {
        // First scan something to populate the store
        ExecutionResult scanResult = execute("scan", "-f", RULES_DIRECTORY);
        assertThat(scanResult.getExitCode()).isZero();

        // Execute with output file
        File outputFile = new File(getWorkingDirectory(), "output.txt");
        ExecutionResult execResult = execute("exec", "-q", "RETURN 42 as answer",
            "-Djqassistant.exec.output-file=" + outputFile.getAbsolutePath());
        assertThat(execResult.getExitCode()).isZero();

        // Verify output was written to file
        assertThat(outputFile).exists();
        String content = java.nio.file.Files.readString(outputFile.toPath());
        assertThat(content).contains("answer");
        assertThat(content).contains("42");
    }

    @DistributionTest
    void execWithoutQuery() {
        // Execute without query should fail
        ExecutionResult execResult = execute("exec");
        assertThat(execResult.getExitCode()).isNotZero();

        List<String> errorConsole = execResult.getErrorConsole();
        assertThat(errorConsole).anyMatch(line -> line.contains("query") || line.contains("must be specified"));
    }
}
