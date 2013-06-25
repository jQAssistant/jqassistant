package com.buschmais.jqassistant.test.javaee6_web.persistence.api;

import com.buschmais.jqassistant.test.javaee6_web.persistence.api.model.Person;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
public interface PersonDAO {

    void create(Person person);

    Person update(Person person);

    void delete(Person person);

    List<Person> findAll();

}
