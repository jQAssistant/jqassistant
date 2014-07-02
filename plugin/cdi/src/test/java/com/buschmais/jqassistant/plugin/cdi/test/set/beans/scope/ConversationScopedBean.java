package com.buschmais.jqassistant.plugin.cdi.test.set.beans.scope;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Produces;

@ConversationScoped
public class ConversationScopedBean implements Serializable {

    @Produces
    @ConversationScoped
    private String producerField;

    @Produces
    @ConversationScoped
    public String producerMethod() {
        return "value";
    }
}
