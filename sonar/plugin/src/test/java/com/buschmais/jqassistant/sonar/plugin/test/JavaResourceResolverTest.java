package com.buschmais.jqassistant.sonar.plugin.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sonar.api.resources.JavaFile;

import com.buschmais.jqassistant.sonar.plugin.language.JavaResourceResolver;

public class JavaResourceResolverTest {

    private JavaResourceResolver resourceResolver = new JavaResourceResolver();

    @Test
    public void type() {
        JavaFile javaFile = (JavaFile) resourceResolver.resolve("Type", JavaResourceResolverTest.class.getName());
        assertThat(javaFile.getLongName(), equalTo(JavaResourceResolverTest.class.getName()));
    }

}
