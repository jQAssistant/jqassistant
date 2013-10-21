package com.buschmais.jqassistant.test.javaee6.backend.cart.persistence.api.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.buschmais.jqassistant.test.javaee6.backend.user.persistence.api.model.User;

/**
 * Created with IntelliJ IDEA. User: dirk.mahler Date: 24.06.13 Time: 14:08 To
 * change this template use File | Settings | File Templates.
 */
@Entity
public class Cart {

	@Id
	@SequenceGenerator(name = "PERSON_SEQ")
	@GeneratedValue(generator = "PERSON_SEQ")
	private Long id;

	@NotNull
	@ManyToOne
	private User user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
