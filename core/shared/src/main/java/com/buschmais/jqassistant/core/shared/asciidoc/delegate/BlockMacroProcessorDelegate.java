package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.BlockMacroProcessor;

public class BlockMacroProcessorDelegate extends BlockMacroProcessor {

    private final BlockMacroProcessor delegate;

    public BlockMacroProcessorDelegate(BlockMacroProcessor delegate) {
        super(delegate.getName());
        this.delegate = delegate;
    }

    @Override
    public Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
        return delegate.process(parent, target, attributes);
    }
}
