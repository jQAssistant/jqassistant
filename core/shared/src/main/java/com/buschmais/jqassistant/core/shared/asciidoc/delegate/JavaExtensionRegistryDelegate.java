package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import org.asciidoctor.extension.*;

class JavaExtensionRegistryDelegate extends JavaExtensionRegistry {

    private JavaExtensionRegistry delegate;

    JavaExtensionRegistryDelegate(JavaExtensionRegistry delegate) {
        super(null, null);
        this.delegate = delegate;
    }

    @Override
    public void docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor) {
        delegate.docinfoProcessor(docInfoProcessor);
    }

    @Override
    public void docinfoProcessor(DocinfoProcessor docInfoProcessor) {
        delegate.docinfoProcessor(new DocInfoProcessorDelegate(docInfoProcessor));
    }

    @Override
    public void docinfoProcessor(String docInfoProcessor) {
        delegate.docinfoProcessor(docInfoProcessor);
    }

    @Override
    public void preprocessor(Class<? extends Preprocessor> preprocessor) {
        delegate.preprocessor(preprocessor);
    }

    @Override
    public void preprocessor(Preprocessor preprocessor) {
        delegate.preprocessor(new PreprocessorDelegate(preprocessor));
    }

    @Override
    public void preprocessor(String preprocessor) {
        delegate.preprocessor(preprocessor);
    }

    @Override
    public void postprocessor(String postprocessor) {
        delegate.postprocessor(postprocessor);
    }

    @Override
    public void postprocessor(Class<? extends Postprocessor> postprocessor) {
        delegate.postprocessor(postprocessor);
    }

    @Override
    public void postprocessor(Postprocessor postprocesor) {
        delegate.postprocessor(new PostProcessorDelegate(postprocesor));
    }

    @Override
    public void includeProcessor(String includeProcessor) {
        delegate.includeProcessor(includeProcessor);
    }

    @Override
    public void includeProcessor(Class<? extends IncludeProcessor> includeProcessor) {
        delegate.includeProcessor(includeProcessor);
    }

    @Override
    public void includeProcessor(IncludeProcessor includeProcessor) {
        delegate.includeProcessor(new IncludeProcessorDelegate(includeProcessor));
    }

    @Override
    public void treeprocessor(Treeprocessor treeprocessor) {
        delegate.treeprocessor(new TreeProcessorDelegate(treeprocessor));
    }

    @Override
    public void treeprocessor(Class<? extends Treeprocessor> treeProcessor) {
        delegate.treeprocessor(treeProcessor);
    }

    @Override
    public void treeprocessor(String treeProcessor) {
        delegate.treeprocessor(treeProcessor);
    }

    @Override
    public void block(String blockName, String blockProcessor) {
        delegate.block(blockName, blockProcessor);
    }

    @Override
    public void block(String blockName, Class<? extends BlockProcessor> blockProcessor) {
        delegate.block(blockName, blockProcessor);
    }

    @Override
    public void block(BlockProcessor blockProcessor) {
        delegate.block(new BlockProcessorDelegate(blockProcessor));
    }

    @Override
    public void block(String blockName, BlockProcessor blockProcessor) {
        delegate.block(blockName, blockProcessor);
    }

    @Override
    public void blockMacro(String blockName, Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        delegate.blockMacro(blockName, blockMacroProcessor);
    }

    @Override
    public void blockMacro(String blockName, String blockMacroProcessor) {
        delegate.blockMacro(blockName, blockMacroProcessor);
    }

    @Override
    public void blockMacro(BlockMacroProcessor blockMacroProcessor) {
        delegate.blockMacro(new BlockMacroProcessorDelegate(blockMacroProcessor));
    }

    @Override
    public void inlineMacro(InlineMacroProcessor inlineMacroProcessor) {
        delegate.inlineMacro(new InlineMacroProcessorDelegate(inlineMacroProcessor));
    }

    @Override
    public void inlineMacro(String blockName, Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        delegate.inlineMacro(blockName, inlineMacroProcessor);
    }

    @Override
    public void inlineMacro(String blockName, String inlineMacroProcessor) {
        delegate.inlineMacro(blockName, inlineMacroProcessor);
    }
}
