package com.buschmais.jqassistant.core.mcp.impl;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.model.RuleSetBuilder;
import com.buschmais.jqassistant.core.store.api.Store;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Verifies the {@link MCPServer} by exchanging JSON-RPC messages with it.
 */
@Timeout(30)
class MCPServerTest {

    private final Store store = mock(Store.class);

    private PipedOutputStream clientOutput;

    private BufferedReader clientInput;

    private MCPServer mcpServer;

    private Thread serverThread;

    private CompletableFuture<Void> serverResult;

    @BeforeEach
    void setUp() throws IOException, RuleException {
        RuleSet ruleSet = RuleSetBuilder.newInstance()
            .addConcept(Concept.builder()
                .id("java:Type")
                .description("Labels all types.")
                .build())
            .getRuleSet();
        clientOutput = new PipedOutputStream();
        PipedInputStream serverInput = new PipedInputStream(clientOutput);
        PipedInputStream clientPipe = new PipedInputStream();
        PipedOutputStream serverOutput = new PipedOutputStream(clientPipe);
        clientInput = new BufferedReader(new InputStreamReader(clientPipe, UTF_8));
        mcpServer = new MCPServer(store, ruleSet, serverInput, serverOutput);
        serverResult = new CompletableFuture<>();
        serverThread = new Thread(() -> {
            try {
                mcpServer.run();
                serverResult.complete(null);
            } catch (Throwable t) {
                serverResult.completeExceptionally(t);
            }
        });
        serverThread.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (serverThread.isAlive()) {
            serverThread.interrupt();
        }
        clientOutput.close();
    }

    @Test
    void initializeAndListTools() throws IOException {
        JsonNode initializeResponse = initialize();

        assertThat(initializeResponse.at("/result/serverInfo/name")
            .asString()).isEqualTo("jQAssistant MCP Server");
        assertThat(initializeResponse.at("/result/instructions")
            .asString()).contains("get-schema", "get-concepts", "execute-cypher");

        JsonNode toolsResponse = request(2, "tools/list", "{}");

        assertThat(toolsResponse.at("/result/tools")
            .valueStream()
            .map(tool -> tool.get("name")
                .asString())).containsExactlyInAnyOrder("get-schema", "get-concepts", "execute-cypher");
    }

    @Test
    void callTool() throws IOException {
        initialize();

        JsonNode callResponse = request(2, "tools/call", "{\"name\":\"get-concepts\",\"arguments\":{}}");

        assertThat(callResponse.at("/result/structuredContent/concepts/0/id")
            .asString()).isEqualTo("java:Type");
        assertThat(callResponse.at("/result/structuredContent/concepts/0/description")
            .asString()).isEqualTo("Labels all types.");
    }

    @Test
    void stopsWhenClientClosesConnection() throws Exception {
        initialize();

        clientOutput.close();

        serverResult.get(10, TimeUnit.SECONDS);
        serverThread.join(10_000);
        assertThat(serverThread.isAlive()).isFalse();
    }

    @Test
    void stopsWhenInterrupted() {
        serverThread.interrupt();

        assertThatThrownBy(() -> serverResult.get(10, TimeUnit.SECONDS)).hasCauseInstanceOf(InterruptedException.class);
    }

    @Test
    void doesNotManageStoreLifecycle() throws Exception {
        initialize();
        clientOutput.close();
        serverResult.get(10, TimeUnit.SECONDS);

        verify(store, never()).start();
        verify(store, never()).stop();
    }

    private JsonNode initialize() throws IOException {
        JsonNode response = request(1, "initialize",
            "{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\",\"version\":\"1.0\"}}");
        send("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        return response;
    }

    private JsonNode request(int id, String method, String params) throws IOException {
        send("{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"method\":\"" + method + "\",\"params\":" + params + "}");
        String line = clientInput.readLine();
        assertThat(line).as("Response to " + method)
            .isNotNull();
        JsonNode response = JsonMapper.shared()
            .readTree(line);
        assertThat(response.get("id")
            .asInt()).isEqualTo(id);
        assertThat(response.has("error")).as("Error response: " + line)
            .isFalse();
        return response;
    }

    private void send(String message) throws IOException {
        clientOutput.write((message + "\n").getBytes(UTF_8));
        clientOutput.flush();
    }
}
