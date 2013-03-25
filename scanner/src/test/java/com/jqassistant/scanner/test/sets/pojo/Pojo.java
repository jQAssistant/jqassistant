package com.jqassistant.scanner.test.sets.pojo;

import java.util.Map;
import java.util.Set;

public class Pojo<X> {

	private String stringValue;

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public <Y> void setX(X x, Map<X, Set<Y>> values) {

	}

}
