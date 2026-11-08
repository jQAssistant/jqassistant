package com.buschmais.jqassistant.commandline.task;

import java.io.File;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.commandline.task.mcp.tool.Tool;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.GetSchemaTool;
import com.buschmais.jqassistant.commandline.task.mcp.tool.rules.GetConceptsTool;
import com.buschmais.jqassistant.commandline.task.mcp.tool.store.ExecuteCypherTool;
import com.buschmais.jqassistant.core.runtime.api.bootstrap.VersionProvider;
import com.buschmais.jqassistant.core.store.api.Store;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.commons.cli.Options;
import tools.jackson.databind.json.JsonMapper;

public class MCPTask extends AbstractRuleTask {

    private static final String INSTRUCTIONS = """
        This server provides access to the jQAssistant graph database (Neo4j) of a project, containing the structures created by scanner plugins (e.g. Java classes, Maven modules, XML files) and their enrichments created by rules during analysis.
        Recommended workflow:
        1. Use get-schema to discover the graph model created by the scanner plugins, i.e. node labels, their properties and outgoing relationships.
        2. Use get-concepts to discover the concepts, which add further labels, properties and relationships (e.g. identifying test classes or architectural layers) if they have been applied during analysis.
        3. Use execute-cypher to query the graph based on the discovered labels, properties and relationships. Prefer read-only queries and use LIMIT for potentially large results.
        """;

    private final JacksonMcpJsonMapper mcpJsonMapper = new JacksonMcpJsonMapper(JsonMapper.shared());

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        Store store = storeFactory.getStore(configuration.store(), () -> new File(projectDirectory, DEFAULT_STORE_DIRECTORY));
        store.start();

        McpServerFeatures.SyncToolSpecification getSchemaToolSpec = getToolSpec(new GetSchemaTool(store, mcpJsonMapper));
        McpServerFeatures.SyncToolSpecification executeCypherToolSpec = getToolSpec(new ExecuteCypherTool(store, mcpJsonMapper));
        McpServerFeatures.SyncToolSpecification getConceptsToolSpec = getToolSpec(new GetConceptsTool(getAvailableRules(configuration.analyze()
            .rule()), mcpJsonMapper));

        StdioServerTransportProvider stdioServerTransportProvider = new StdioServerTransportProvider(mcpJsonMapper);
        McpSyncServer mcpSyncServer = McpServer.sync(stdioServerTransportProvider)
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

        Runtime.getRuntime()
            .addShutdownHook(new Thread(() -> {
                store.stop();
                mcpSyncServer.closeGracefully();
            }));
    }

    private McpServerFeatures.SyncToolSpecification getToolSpec(Tool tool) {
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

