package com.buschmais.jqassistant.test.javaee6.cart.ui;

import com.buschmais.jqassistant.test.javaee6.cart.logic.api.CartService;
import com.buschmais.jqassistant.test.javaee6.cart.persistence.api.model.Cart;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
@Named
@ConversationScoped
public class CartBean implements Serializable {

    @Inject
    private CartService cartService;

    @Inject
    private Conversation conversation;

    private Cart cart;

    public List<Cart> getPersonList() {
        return cartService.getPersons();
    }

    public String onCreate() {
        this.cart = new Cart();
        this.conversation.begin();
        return "/edit";
    }

    public String onEdit(Cart cart) {
        this.cart = cart;
        this.conversation.begin();
        return "/edit";
    }

    public String onSave() {
        this.conversation.end();
        if (this.cart.getId() == null) {
            this.cartService.create(this.cart);
        } else {
            this.cartService.update(this.cart);
        }
        return "/list";
    }

    public String onDelete(Cart cart) {
        this.cartService.delete(cart);
        return "/list";
    }

}
