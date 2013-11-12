package com.buschmais.jqassistant.test.javaee6.backend.cart.logic.api;

import java.io.Serializable;
import java.util.List;

import com.buschmais.jqassistant.test.javaee6.backend.cart.model.api.model.Cart;

/**
 * Created with IntelliJ IDEA. User: dirk.mahler Date: 24.06.13 Time: 14:23 To
 * change this template use File | Settings | File Templates.
 */
public interface CartService extends Serializable {

	void create(Cart cart);

	Cart update(Cart cart);

	void delete(Cart cart);

	List<Cart> getPersons();
}
