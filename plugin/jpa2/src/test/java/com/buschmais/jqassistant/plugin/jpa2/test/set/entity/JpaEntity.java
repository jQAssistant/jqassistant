package com.buschmais.jqassistant.plugin.jpa2.test.set.entity;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * A JPA entity
 */
@Entity
@NamedQueries(@NamedQuery(name = JpaEntity.TESTQUERY_NAME, query = JpaEntity.TESTQUERY_QUERY))
public class JpaEntity {

    public static final String TESTQUERY_NAME = "testQuery";
    public static final String TESTQUERY_QUERY = "SELECT e FROM JpaEntity e";

}
