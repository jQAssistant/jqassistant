package com.buschmais.jqassistant.test.javaee6_web.persistence.impl;

import com.buschmais.jqassistant.test.javaee6_web.persistence.api.PersonDAO;
import com.buschmais.jqassistant.test.javaee6_web.persistence.api.model.Person;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
public class PersonDAOImpl implements PersonDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void create(Person person) {
        entityManager.persist(person);
    }

    @Override
    public Person update(Person person) {
        return entityManager.merge(person);
    }

    @Override
    public void delete(Person person) {
        entityManager.remove(person);
    }

    @Override
    public List<Person> findAll() {
        return entityManager.createQuery("SELECT p FROM Person p", Person.class).getResultList();
    }
}
