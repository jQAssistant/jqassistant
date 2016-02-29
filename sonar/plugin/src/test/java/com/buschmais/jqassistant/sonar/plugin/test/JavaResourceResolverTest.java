package com.buschmais.jqassistant.sonar.plugin.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

import com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement;
import com.buschmais.jqassistant.sonar.plugin.language.JavaResourceResolver;

@RunWith(MockitoJUnitRunner.class)
public class JavaResourceResolverTest {

	@Mock
	private FileSystem fileSystem;

	@Mock
	private Project project;

	@Mock
	FilePredicates predicates;

	@Test
	public void type() {
		java.io.File javaFile = new File(JavaResourceResolverTest.class.getName().replace('.', '/').concat(".java"));
		Iterable<InputFile> it = Collections.singletonList((InputFile)new DefaultInputFile(null, javaFile.getPath()));
		when(fileSystem.predicates()).thenReturn(predicates);
		when(fileSystem.inputFiles(Matchers.any(FilePredicate.class))).thenReturn(it);
		JavaResourceResolver resourceResolver = new JavaResourceResolver(fileSystem);
		Resource result = resourceResolver.resolve(project, JavaLanguageElement.Type.name(), JavaResourceResolverTest.class.getName().replace('.', '/').concat(".class"), JavaResourceResolverTest.class.getName());
		assertEquals(result.getPath(), javaFile.getPath());
	}

}
