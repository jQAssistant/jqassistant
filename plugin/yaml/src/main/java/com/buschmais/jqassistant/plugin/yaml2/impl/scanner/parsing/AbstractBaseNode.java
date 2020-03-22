package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

public abstract class AbstractBaseNode {
    private int tokenIndex;

    public AbstractBaseNode(int tokenIndex) {
        this.tokenIndex = tokenIndex;
    }


    public int getTokenIndex() {
        return tokenIndex;
    }

    @Override
    public String toString() {
        return generateTextPresentation();
    }

    protected abstract String generateTextPresentation();
}
