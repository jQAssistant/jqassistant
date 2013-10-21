package com.buschmais.jqassistant.test.javaee6.backend.cart.logic.impl;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;

import com.buschmais.jqassistant.test.javaee6.backend.cart.logic.api.CartService;
import com.buschmais.jqassistant.test.javaee6.backend.cart.persistence.api.CartDAO;
import com.buschmais.jqassistant.test.javaee6.backend.cart.persistence.api.model.Cart;

/**
 * Created with IntelliJ IDEA. User: dirk.mahler Date: 24.06.13 Time: 14:24 To
 * change this template use File | Settings | File Templates.
 */
@Stateless
@Local(CartService.class)
public class CartBean implements CartService {

	private static final long serialVersionUID = 1L;
	private CartDAO cartDAO;

	@Override
	public void create(Cart cart) {
		cartDAO.create(cart);
	}

	@Override
	public Cart update(Cart cart) {
		return cartDAO.update(cart);
	}

	@Override
	public void delete(Cart cart) {
		cartDAO.delete(cart);
	}

	@Override
	public List<Cart> getPersons() {
		return cartDAO.findAll();
	}
}
