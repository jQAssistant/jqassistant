package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import org.asciidoctor.extension.*;

class JavaExtensionRegistryDelegate implements JavaExtensionRegistry {

    private JavaExtensionRegistry delegate;

    JavaExtensionRegistryDelegate(JavaExtensionRegistry delegate) {
        this.delegate = delegate;
    }

    @Override
    public JavaExtensionRegistry docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor) {
        delegate.docinfoProcessor(docInfoProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry docinfoProcessor(DocinfoProcessor docInfoProcessor) {
        delegate.docinfoProcessor(new DocInfoProcessorDelegate(docInfoProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry docinfoProcessor(String docInfoProcessor) {
        delegate.docinfoProcessor(docInfoProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry preprocessor(Class<? extends Preprocessor> preprocessor) {
        delegate.preprocessor(preprocessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry preprocessor(Preprocessor preprocessor) {
        delegate.preprocessor(new PreprocessorDelegate(preprocessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry preprocessor(String preprocessor) {
        delegate.preprocessor(preprocessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry postprocessor(String postprocessor) {
        delegate.postprocessor(postprocessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry postprocessor(Class<? extends Postprocessor> postprocessor) {
        delegate.postprocessor(postprocessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry postprocessor(Postprocessor postprocesor) {
        delegate.postprocessor(new PostProcessorDelegate(postprocesor));
        return this;
    }

    @Override
    public JavaExtensionRegistry includeProcessor(String includeProcessor) {
        delegate.includeProcessor(includeProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry includeProcessor(Class<? extends IncludeProcessor> includeProcessor) {
        delegate.includeProcessor(includeProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry includeProcessor(IncludeProcessor includeProcessor) {
        delegate.includeProcessor(new IncludeProcessorDelegate(includeProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry treeprocessor(Treeprocessor treeprocessor) {
        delegate.treeprocessor(new TreeProcessorDelegate(treeprocessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry treeprocessor(Class<? extends Treeprocessor> treeProcessor) {
        delegate.treeprocessor(treeProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry treeprocessor(String treeProcessor) {
        delegate.treeprocessor(treeProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry block(String blockName, String blockProcessor) {
        delegate.block(blockName, blockProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry block(String s) {
        delegate.block(s);
        return this;
    }

    @Override
    public JavaExtensionRegistry block(String blockName, Class<? extends BlockProcessor> blockProcessor) {
        delegate.block(blockName, blockProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry block(Class<? extends BlockProcessor> aClass) {
        block(aClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry block(BlockProcessor blockProcessor) {
        delegate.block(new BlockProcessorDelegate(blockProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry block(String blockName, BlockProcessor blockProcessor) {
        delegate.block(blockName, blockProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(String blockName, Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        delegate.blockMacro(blockName, blockMacroProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(Class<? extends BlockMacroProcessor> aClass) {
        delegate.blockMacro(aClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(String blockName, String blockMacroProcessor) {
        delegate.blockMacro(blockName, blockMacroProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(String s) {
        delegate.blockMacro(s);
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(BlockMacroProcessor blockMacroProcessor) {
        delegate.blockMacro(new BlockMacroProcessorDelegate(blockMacroProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(String s, BlockMacroProcessor blockMacroProcessor) {
        delegate.blockMacro(s, blockMacroProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(InlineMacroProcessor inlineMacroProcessor) {
        delegate.inlineMacro(new InlineMacroProcessorDelegate(inlineMacroProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String s, InlineMacroProcessor inlineMacroProcessor) {
        delegate.inlineMacro(s, inlineMacroProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String blockName, Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        delegate.inlineMacro(blockName, inlineMacroProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(Class<? extends InlineMacroProcessor> aClass) {
        delegate.inlineMacro(aClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String blockName, String inlineMacroProcessor) {
        delegate.inlineMacro(blockName, inlineMacroProcessor);
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String s) {
        delegate.inlineMacro(s);
        return this;
    }
}
