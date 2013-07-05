package com.buschmais.jqassistant.test.javaee6.user.persistence.api.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class User {

    @Id
    @SequenceGenerator(name = "USER_SEQ")
    @GeneratedValue(generator = "USER_SEQ")
    private Long id;

    @NotNull
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
