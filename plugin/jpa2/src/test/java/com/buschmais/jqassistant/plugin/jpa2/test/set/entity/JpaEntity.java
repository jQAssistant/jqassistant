package com.buschmais.jqassistant.plugin.jpa2.test.set.entity;

import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * A JPA entity.
 */
@Entity
@NamedQueries(@NamedQuery(name = JpaEntity.TESTQUERY_NAME, query = JpaEntity.TESTQUERY_QUERY))
public class JpaEntity {

    public static final String TESTQUERY_NAME = "testQuery";
    public static final String TESTQUERY_QUERY = "SELECT e FROM JpaEntity e";

    @Id
    @EmbeddedId
    private int id;

    private JpaEmbeddable embeddable;

    @Embedded
    private JpaEmbedded embedded;

    @EmbeddedId
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JpaEmbeddable getEmbeddable() {
        return embeddable;
    }

    public void setEmbeddable(JpaEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

    @Embedded
    public JpaEmbedded getEmbedded() {
        return embedded;
    }

    public void setEmbedded(JpaEmbedded embedded) {
        this.embedded = embedded;
    }
}
