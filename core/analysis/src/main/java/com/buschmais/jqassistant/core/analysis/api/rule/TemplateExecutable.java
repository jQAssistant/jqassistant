package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Defines a reference to a template which may be executed.
 */
public class TemplateExecutable implements Executable {

    private String templateId;

    public TemplateExecutable(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }
}
