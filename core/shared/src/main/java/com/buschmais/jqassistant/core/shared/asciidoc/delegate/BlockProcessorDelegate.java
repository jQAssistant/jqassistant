package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import java.util.Map;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Reader;

public class BlockProcessorDelegate extends BlockProcessor {

    private final BlockProcessor delegate;

    public BlockProcessorDelegate(BlockProcessor delegate) {
        super(delegate.getName());
        this.delegate = delegate;
    }

    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        return delegate.process(parent, reader, attributes);
    }
}
