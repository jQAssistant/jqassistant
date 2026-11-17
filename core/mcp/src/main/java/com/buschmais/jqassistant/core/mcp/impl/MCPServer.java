package com.buschmais.jqassistant.core.mcp.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;

import com.buschmais.jqassistant.core.mcp.impl.tool.getschema.GetSchemaTool;
import com.buschmais.jqassistant.core.mcp.impl.tool.rules.GetConceptsTool;
import com.buschmais.jqassistant.core.mcp.impl.tool.store.ExecuteCypherTool;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.runtime.api.bootstrap.VersionProvider;
import com.buschmais.jqassistant.core.store.api.Store;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

/**
 * MCP server providing access to the jQAssistant store and rules.
 */
@Slf4j
public class MCPServer {

    private static final String INSTRUCTIONS = """
        This server provides access to the jQAssistant graph database (Neo4j) of a project, containing the structures created by scanner plugins (e.g. Java classes, Maven modules, XML files) and their enrichments created by rules during analysis.
        Recommended workflow:
        1. Use get-schema to discover the graph model created by the scanner plugins, i.e. node labels, their properties and outgoing relationships.
        2. Use get-concepts to discover the concepts, which add further labels, properties and relationships (e.g. identifying test classes or architectural layers) if they have been applied during analysis.
        3. Use execute-cypher to query the graph based on the discovered labels, properties and relationships. Prefer read-only queries and use LIMIT for potentially large results.
        """;

    private final JacksonMcpJsonMapper mcpJsonMapper = new JacksonMcpJsonMapper(JsonMapper.shared());

    private final Store store;

    private final RuleSet ruleSet;

    private final InputStream input;

    private final OutputStream output;

    private final boolean redirectSystemOut;

    /**
     * Creates an MCP server using stdio, i.e. {@link System#in} and {@link System#out}.
     * <p>
     * While the server is running {@link System#out} is redirected to {@link System#err} as stdout is reserved for the MCP protocol.
     *
     * @param store
     *     The {@link Store}, must be started.
     * @param ruleSet
     *     The {@link RuleSet}.
     */
    public MCPServer(Store store, RuleSet ruleSet) {
        this(store, ruleSet, System.in, System.out, true);
    }

    /**
     * Creates an MCP server using the given streams.
     *
     * @param store
     *     The {@link Store}, must be started.
     * @param ruleSet
     *     The {@link RuleSet}.
     * @param input
     *     The {@link InputStream} to read MCP messages from.
     * @param output
     *     The {@link OutputStream} to write MCP messages to.
     */
    public MCPServer(Store store, RuleSet ruleSet, InputStream input, OutputStream output) {
        this(store, ruleSet, input, output, false);
    }

    private MCPServer(Store store, RuleSet ruleSet, InputStream input, OutputStream output, boolean redirectSystemOut) {
        this.store = store;
        this.ruleSet = ruleSet;
        this.input = input;
        this.output = output;
        this.redirectSystemOut = redirectSystemOut;
    }

    /**
     * Runs the MCP server and blocks until the client closes the connection (i.e. end of input) or the current thread is interrupted.
     * <p>
     * The lifecycle of the {@link Store} is not managed by the server, i.e. it is neither started nor stopped.
     *
     * @throws InterruptedException
     *     If the current thread has been interrupted while waiting.
     */
    public void run() throws InterruptedException {
        PrintStream systemOut = System.out;
        if (redirectSystemOut) {
            System.setOut(System.err);
        }
        try {
            CountDownLatch inputClosed = new CountDownLatch(1);
            McpSyncServer mcpSyncServer = createServer(new StdioServerTransportProvider(mcpJsonMapper, new CloseAwareInputStream(input, inputClosed::countDown),
                output));
            log.info("jQAssistant MCP server started.");
            try {
                inputClosed.await();
                log.info("Input closed, stopping jQAssistant MCP server.");
            } finally {
                mcpSyncServer.closeGracefully();
            }
        } finally {
            if (redirectSystemOut) {
                System.setOut(systemOut);
            }
        }
    }

    private McpSyncServer createServer(StdioServerTransportProvider transportProvider) {
        McpServerFeatures.SyncToolSpecification getSchemaToolSpec = getToolSpec(new GetSchemaTool(store, mcpJsonMapper));
        McpServerFeatures.SyncToolSpecification executeCypherToolSpec = getToolSpec(new ExecuteCypherTool(store, mcpJsonMapper));
        McpServerFeatures.SyncToolSpecification getConceptsToolSpec = getToolSpec(new GetConceptsTool(ruleSet, mcpJsonMapper));
        return McpServer.sync(transportProvider)
            .serverInfo("jQAssistant MCP Server", VersionProvider.getVersionProvider()
                .getVersion())
            .capabilities(McpSchema.ServerCapabilities.builder()
                .resources(false, false)
                .tools(false)
                .prompts(false)
                .logging()
                .build())
            .instructions(INSTRUCTIONS)
            .tools(getSchemaToolSpec, executeCypherToolSpec, getConceptsToolSpec)
            .build();
    }

    private McpServerFeatures.SyncToolSpecification getToolSpec(com.buschmais.jqassistant.core.mcp.impl.tool.Tool tool) {
        McpSchema.Tool.Builder toolBuilder = McpSchema.Tool.builder(tool.getName(), mcpJsonMapper, tool.getInputSchema())
            .description(tool.getDescription())
            .annotations(tool.getAnnotations());
        tool.getOutputSchema()
            .ifPresent(outputSchema -> toolBuilder.outputSchema(mcpJsonMapper, outputSchema));
        return McpServerFeatures.SyncToolSpecification.builder()
            .tool(toolBuilder.build())
            .callHandler((exchange, request) -> McpSchema.CallToolResult.builder()
                .structuredContent(tool.execute(request.arguments()))
                .build())
            .build();
    }
}

