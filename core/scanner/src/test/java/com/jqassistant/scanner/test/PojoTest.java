package com.jqassistant.scanner.test;

import java.io.IOException;

import org.junit.Test;

import com.jqassistant.scanner.test.sets.pojo.Pojo;

public class PojoTest extends AbstractScannerTest {

	@Test
	public void attributes() throws IOException {
		scanner.scanClass(Pojo.class);
	}

}
