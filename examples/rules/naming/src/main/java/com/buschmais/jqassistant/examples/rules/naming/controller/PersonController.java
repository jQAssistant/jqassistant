package com.buschmais.jqassistant.examples.rules.naming.controller;

import com.buschmais.jqassistant.examples.rules.naming.Controller;
import com.buschmais.jqassistant.examples.rules.naming.model.PersonModel;

/**
 * {@link Controller} implementation for the
 * {@link com.buschmais.jqassistant.examples.rules.naming.model.PersonModel}.
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
