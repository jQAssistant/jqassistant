package com.buschmais.jqassistant.plugin.javaee6.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.javaee6.impl.scanner.WebApplicationFileResolverStrategy;

/**
 * Defines the scopes for Java web applications.
 */
public enum WebApplicationScope implements Scope {

    WAR {
        @Override
        public void create(ScannerContext context) {
            context.peek(FileResolver.class).push(new WebApplicationFileResolverStrategy());
        }

        @Override
        public void destroy(ScannerContext context) {
            context.peek(FileResolver.class).pop();
        }
    };

    @Override
    public String getPrefix() {
        return "java-web";
    }

    @Override
    public String getName() {
        return name();
    }

}
