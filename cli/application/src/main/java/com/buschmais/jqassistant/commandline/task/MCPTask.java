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

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        Store store = storeFactory.getStore(configuration.store(), () -> new File(projectDirectory, DEFAULT_STORE_DIRECTORY));
        store.start();

        JacksonMcpJsonMapper mcpJsonMapper = new JacksonMcpJsonMapper(JsonMapper.shared());

        McpServerFeatures.SyncToolSpecification getSchemaToolSpec = getToolSpec(new GetSchemaTool(store, mcpJsonMapper), mcpJsonMapper);
        McpServerFeatures.SyncToolSpecification executeCypherToolSpec = getToolSpec(new ExecuteCypherTool(store), mcpJsonMapper);
        McpServerFeatures.SyncToolSpecification getConceptsToolSpec = getToolSpec(new GetConceptsTool(getAvailableRules(configuration.analyze()
            .rule())), mcpJsonMapper);

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
            .tools(getSchemaToolSpec, executeCypherToolSpec, getConceptsToolSpec)
            .build();

        Runtime.getRuntime()
            .addShutdownHook(new Thread(() -> {
                store.stop();
                mcpSyncServer.closeGracefully();
            }));
    }

    private static McpServerFeatures.SyncToolSpecification getToolSpec(Tool tool, JacksonMcpJsonMapper mcpJsonMapper) {
        return McpServerFeatures.SyncToolSpecification.builder()
            .tool(McpSchema.Tool.builder(tool.getName(), mcpJsonMapper, tool.getInputSchema())
                .description(tool.getDescription())
                .build())
            .callHandler((exchange, request) -> McpSchema.CallToolResult.builder()
                .structuredContent(tool.execute(request.arguments()))
                .build())
            .build();
    }
}

