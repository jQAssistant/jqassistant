package com.buschmais.jqassistant.scanner.test;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.scanner.test.sets.pojo.Pojo;

public class PojoTest extends AbstractScannerIT {

	@Test
	public void attributes() throws IOException {
		scanner.scanClass(Pojo.class);
	}

}
