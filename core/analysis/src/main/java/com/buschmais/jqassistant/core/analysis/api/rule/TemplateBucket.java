package com.buschmais.jqassistant.core.analysis.api.rule;

public class TemplateBucket extends AbstractRuleBucket<Template, NoTemplateException, DuplicateTemplateException> {
    @Override
    protected String getRuleTypeName() {
        return "template";
    }

    @Override
    protected DuplicateTemplateException newDuplicateRuleException(String message) {
        return new DuplicateTemplateException(message);
    }

    @Override
    protected NoTemplateException newNoRuleException(String message) {
        return new NoTemplateException(message);
    }

    public Template getTemplate(String id) throws NoTemplateException {
        return get(id);
    }

    public void addTemplates(TemplateBucket bucket) throws DuplicateTemplateException {
        addAll(bucket);
    }
}
