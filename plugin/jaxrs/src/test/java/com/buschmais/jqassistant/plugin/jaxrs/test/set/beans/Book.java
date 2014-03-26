package com.buschmais.jqassistant.plugin.jaxrs.test.set.beans;

import java.io.Serializable;

/**
 * A simple DTO.
 * 
 * @author Aparna Chaudhary
 */
public class Book implements Serializable {

	private static final long serialVersionUID = -76950387741972854L;

	private String title;
	private String author;
	private String isbn;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

}
