package com.buschmais.jqassistant.test.javaee6_web.logic.api;

import com.buschmais.jqassistant.test.javaee6_web.persistence.api.model.Person;

import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:23
 * To change this template use File | Settings | File Templates.
 */
public interface PersonService extends Serializable {

    void create(Person person);

    Person update(Person person);

    void delete(Person person);

    List<Person> getPersons();
}
