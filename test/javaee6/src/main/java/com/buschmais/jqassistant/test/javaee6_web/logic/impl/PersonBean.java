package com.buschmais.jqassistant.test.javaee6_web.logic.impl;

import com.buschmais.jqassistant.test.javaee6_web.logic.api.PersonService;
import com.buschmais.jqassistant.test.javaee6_web.persistence.api.PersonDAO;
import com.buschmais.jqassistant.test.javaee6_web.persistence.api.model.Person;

import javax.ejb.Local;
import javax.ejb.Stateless;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:24
 * To change this template use File | Settings | File Templates.
 */
@Stateless
@Local(PersonService.class)
public class PersonBean implements PersonService {

    private PersonDAO personDAO;

    @Override
    public void create(Person person) {
        personDAO.create(person);
    }

    @Override
    public Person update(Person person) {
        return personDAO.update(person);
    }

    @Override
    public void delete(Person person) {
        personDAO.delete(person);
    }

    @Override
    public List<Person> getPersons() {
        return personDAO.findAll();
    }
}
