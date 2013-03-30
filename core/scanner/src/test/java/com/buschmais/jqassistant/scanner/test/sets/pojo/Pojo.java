package com.buschmais.jqassistant.scanner.test.sets.pojo;

import java.io.IOException;

public class Pojo<X> {

	private String stringValue;

	private int intValue;

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) throws IOException {
		this.stringValue = stringValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

}
