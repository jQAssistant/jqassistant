package com.buschmais.jqassistant.examples.namingrules.model;

import com.buschmais.jqassistant.examples.namingrules.Model;

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
