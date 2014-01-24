package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.eclipse.tycho.core.TychoConstants;
import org.eclipse.tycho.core.facade.BuildProperties;
import org.eclipse.tycho.core.osgitools.project.EclipsePluginProject;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.ProjectDescriptor;

@RunWith(Parameterized.class)
public class TychoProjectScannerPluginAdditionalFilesTest {

	private final static Class<?> clazz = TychoProjectScannerPluginAdditionalFilesTest.class;

	private final Store store;
	private final MavenProject project;
	private final Matcher<? super Collection<? extends File>> matcher;

	@Parameters
	public static List<Object[]> data() {
		Object[] nothingSelected = new Object[] { new ArrayList<String>(), new ArrayList<String>(), is(empty()) };
		Object[] oneFileSelected = new Object[] { Collections.singletonList("log"), new ArrayList<String>(),
				hasItems(new File(clazz.getResource("log").getFile())) };
		Object[] oneFileAndFolderSelected = new Object[] { Arrays.asList(new String[] { "log", "cache" }), new ArrayList<String>(),
				hasItems(new File(clazz.getResource("log").getFile())) };
		Object[] oneFileExcluded = new Object[] { Collections.singletonList("log"), Collections.singletonList("log"), is(empty()) };

		return Arrays.asList(new Object[][] { nothingSelected, oneFileSelected, oneFileAndFolderSelected, oneFileExcluded });
	}

	public TychoProjectScannerPluginAdditionalFilesTest(List<String> includes, List<String> excludes,
			Matcher<? super Collection<? extends File>> matcher) {
		this.store = mock(Store.class);
		this.project = mock(MavenProject.class);
		this.matcher = matcher;

		EclipsePluginProject pdeProject = mock(EclipsePluginProject.class);
		BuildProperties properties = mock(BuildProperties.class);
		when(properties.getBinExcludes()).thenReturn(excludes);
		when(properties.getBinIncludes()).thenReturn(includes);

		when(project.getContextValue(TychoConstants.CTX_ECLIPSE_PLUGIN_PROJECT)).thenReturn(pdeProject);
		when(pdeProject.getBuildProperties()).thenReturn(properties);
		when(project.getBasedir()).thenReturn(new File(getClass().getResource(".").getFile()));
	}

	@Test
	public void testGetAdditionalFiles() throws Exception {
		ProjectDescriptor scanProject = new TychoProjectScannerPlugin().scanProject(store, project);
		assertThat(scanProject.getAdditionalFiles(), matcher);
	}
}
