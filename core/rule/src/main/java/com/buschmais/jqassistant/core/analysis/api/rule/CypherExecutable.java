package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;
import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

/**
 * Defines a cypher statement which may be executed.
 */
@Deprecated
@ToBeRemovedInVersion(major = 1, minor = 5)
public class CypherExecutable extends SourceExecutable<String> {

    private static final String LANGUAGE = "cypher";

    public CypherExecutable(String statement) {
        super(LANGUAGE, statement);
    }

    @Deprecated
    public String getStatement() {
        return getSource();
    }

}
