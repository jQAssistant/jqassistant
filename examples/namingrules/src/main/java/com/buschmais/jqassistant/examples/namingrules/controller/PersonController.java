package com.buschmais.jqassistant.examples.namingrules.controller;

import com.buschmais.jqassistant.examples.namingrules.Controller;
import com.buschmais.jqassistant.examples.namingrules.model.PersonModel;

/**
 * {@link Controller} implementation for the {@link PersonModel}.
 */
public class PersonController implements Controller {

    private PersonModel personModel;

    public PersonController(PersonModel personModel) {
        this.personModel = personModel;
    }

    public PersonModel getPersonModel() {
        return personModel;
    }
}
