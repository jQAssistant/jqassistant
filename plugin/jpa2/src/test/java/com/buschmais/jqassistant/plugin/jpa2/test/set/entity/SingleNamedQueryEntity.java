package com.buschmais.jqassistant.plugin.jpa2.test.set.entity;

import org.neo4j.cypher.internal.compiler.v2_2.functions.Sin;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;

@Entity
@NamedQuery(name = SingleNamedQueryEntity.TESTQUERY_NAME, query = SingleNamedQueryEntity.TESTQUERY_QUERY)
public class SingleNamedQueryEntity {

    public static final String TESTQUERY_NAME = "namedQuery";
    public static final String TESTQUERY_QUERY = "SELECT e FROM SingleNamedQueryEntity e";

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
