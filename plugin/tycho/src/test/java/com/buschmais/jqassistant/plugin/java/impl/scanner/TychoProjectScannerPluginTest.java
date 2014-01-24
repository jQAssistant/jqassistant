package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.apache.maven.project.MavenProject;
import org.junit.Test;

import com.buschmais.jqassistant.core.store.api.Store;

public class TychoProjectScannerPluginTest {

	@Test
	public void testCreatePlugin() throws Exception {
		assertNotNull(new TychoProjectScannerPlugin());
	}

	@Test
	public void testEmptyScan() throws Exception {
		Store store = mock(Store.class);
		MavenProject project = mock(MavenProject.class);
		new TychoProjectScannerPlugin().scanProject(store, project);
	}
}
