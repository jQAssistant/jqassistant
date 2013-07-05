package com.buschmais.jqassistant.test.javaee6.user.logic.impl;

import com.buschmais.jqassistant.test.javaee6.user.logic.api.UserService;
import com.buschmais.jqassistant.test.javaee6.user.persistence.api.UserDAO;
import com.buschmais.jqassistant.test.javaee6.user.persistence.api.model.User;

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
@Local(UserService.class)
public class UserBean implements UserService {

    private UserDAO userDAO;

    @Override
    public void create(User user) {
        userDAO.create(user);
    }

    @Override
    public User update(User user) {
        return userDAO.update(user);
    }

    @Override
    public void delete(User user) {
        userDAO.delete(user);
    }

    @Override
    public List<User> getUsers() {
        return userDAO.findAll();
    }
}
