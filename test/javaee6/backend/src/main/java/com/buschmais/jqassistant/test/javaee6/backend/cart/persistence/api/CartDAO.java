package com.buschmais.jqassistant.test.javaee6.backend.cart.persistence.api;

import com.buschmais.jqassistant.test.javaee6.backend.cart.persistence.api.model.Cart;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
public interface CartDAO {

    void create(Cart cart);

    Cart update(Cart cart);

    void delete(Cart cart);

    List<Cart> findAll();

}
