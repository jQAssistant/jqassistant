package com.buschmais.jqassistant.test.javaee6.cart.logic.api;

import com.buschmais.jqassistant.test.javaee6.cart.persistence.api.model.Cart;

import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:23
 * To change this template use File | Settings | File Templates.
 */
public interface CartService extends Serializable {

    void create(Cart cart);

    Cart update(Cart cart);

    void delete(Cart cart);

    List<Cart> getPersons();
}
