package com.buschmais.jqassistant.test.javaee6.backend.cart.model.api;

import java.util.List;

import com.buschmais.jqassistant.test.javaee6.backend.cart.model.api.model.Cart;

/**
 * Created with IntelliJ IDEA. User: dirk.mahler Date: 24.06.13 Time: 14:09 To
 * change this template use File | Settings | File Templates.
 */
public interface CartDAO {

	void create(Cart cart);

	Cart update(Cart cart);

	void delete(Cart cart);

	List<Cart> findAll();

}
