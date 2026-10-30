package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.commandline.task.mcp.tool.Tool;
import com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.GetSchemaTool;
import com.buschmais.jqassistant.commandline.task.mcp.tool.rules.GetConceptsTool;
import com.buschmais.jqassistant.core.runtime.api.bootstrap.VersionProvider;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.commons.cli.Options;
import tools.jackson.databind.json.JsonMapper;

public class MCPTask extends AbstractRuleTask {

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        withStore(configuration, store -> {

            GetConceptsTool getConceptsTool = new GetConceptsTool(getAvailableRules(configuration.analyze()
                .rule()));

            JacksonMcpJsonMapper mcpJsonMapper = new JacksonMcpJsonMapper(JsonMapper.shared());

            McpServerFeatures.SyncToolSpecification getSchemaToolSpec = getToolSpec(new GetSchemaTool(store), mcpJsonMapper);
            McpServerFeatures.SyncToolSpecification getConceptsToolSpec = getToolSpec(getConceptsTool, mcpJsonMapper);

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
                .tools(getSchemaToolSpec, getConceptsToolSpec)
                .build();

            Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> mcpSyncServer.closeGracefully()));
        });
    }

    private static McpServerFeatures.SyncToolSpecification getToolSpec(Tool tool, JacksonMcpJsonMapper mcpJsonMapper) {
        McpServerFeatures.SyncToolSpecification schemaToolSpec = McpServerFeatures.SyncToolSpecification.builder()
            .tool(McpSchema.Tool.builder()
                .name(tool.getName())
                .description(tool.getDescription())
                .inputSchema(mcpJsonMapper, tool.getInputSchema())
                .build())
            .callHandler((exchange, request) -> {
                return McpSchema.CallToolResult.builder()
                    .structuredContent(tool.execute())
                    .build();
            })
            .build();
        return schemaToolSpec;
    }
}

