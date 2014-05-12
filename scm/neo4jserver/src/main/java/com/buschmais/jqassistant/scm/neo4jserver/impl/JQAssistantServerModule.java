package com.buschmais.jqassistant.scm.neo4jserver.impl;

import java.util.Collections;
import java.util.List;

import org.neo4j.server.web.WebServer;

/**
 * Defines the Neo4j server module for jQAssistant.
 */
class JQAssistantServerModule implements org.neo4j.server.modules.ServerModule {
    public static final String MOUNTPOINT_REST = "/jqa/rest";
    public static final String MOUNTPOINT_STATIC = "/jqa";
    public static final String CONTENT_STATIC = "jqa";

    private WebServer webServer;
    private List<String> jaxRsClassNames;

    JQAssistantServerModule(WebServer webServer) {
        this.webServer = webServer;
        jaxRsClassNames = Collections.emptyList();
    }

    @Override
    public void start() {
        webServer.addStaticContent(CONTENT_STATIC, MOUNTPOINT_STATIC);
        // webServer.addJAXRSClasses(jaxRsClassNames, MOUNTPOINT_REST, null);
    }

    @Override
    public void stop() {
        // webServer.removeJAXRSClasses(jaxRsClassNames, MOUNTPOINT_REST);
        webServer.removeStaticContent(CONTENT_STATIC, MOUNTPOINT_STATIC);
    }
}
