package com.buschmais.jqassistant.test.javaee6.user.persistence.api;

import com.buschmais.jqassistant.test.javaee6.user.persistence.api.model.User;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
public interface UserDAO {

    void create(User user);

    User update(User user);

    void delete(User user);

    List<User> findAll();

}
