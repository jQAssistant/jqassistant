package com.buschmais.jqassistant.examples.rules.naming.model;

import com.buschmais.jqassistant.examples.rules.naming.Model;

/**
 * A model class annotated with {@link Model}.
 */
@Model
public class PersonModel {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
