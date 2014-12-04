package com.buschmais.jqassistant.core.scanner.api;

/**
 * Interface defining a scope, e.g. a Java classpath.
 */
public interface Scope {

    enum Default implements Scope {
        NONE {
            @Override
            public String getPrefix() {
                return "default";
            }

            @Override
            public String getName() {
                return name();
            }

            @Override
            public void create(ScannerContext context) {
            }

            @Override
            public void destroy(ScannerContext context) {
            }
        };
    }

    /**
     * Return the prefix of the scope, e.g. "java".
     * 
     * @return The prefix.
     */
    String getPrefix();

    /**
     * Return the name of the scope.
     * 
     * @return The name.
     */
    String getName();

    /**
     * Create the scope.
     * 
     * @param context
     *            The current scanner context.
     */
    void create(ScannerContext context);

    /**
     * Destroy the scope.
     * 
     * @param context
     *            The current scanner context.
     */
    void destroy(ScannerContext context);

}
