package com.buschmais.jqassistant.test.javaee6.backend.user.persistence.api;

import java.util.List;

import com.buschmais.jqassistant.test.javaee6.backend.user.persistence.api.model.User;

/**
 * Created with IntelliJ IDEA. User: dirk.mahler Date: 24.06.13 Time: 14:09 To
 * change this template use File | Settings | File Templates.
 */
public interface UserDAO {

	void create(User user);

	User update(User user);

	void delete(User user);

	List<User> findAll();

}
