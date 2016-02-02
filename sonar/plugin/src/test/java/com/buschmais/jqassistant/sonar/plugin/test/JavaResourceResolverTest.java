package com.buschmais.jqassistant.sonar.plugin.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.sonar.api.resources.File;

import com.buschmais.jqassistant.sonar.plugin.language.JavaResourceResolver;

public class JavaResourceResolverTest {

    private JavaResourceResolver resourceResolver = new JavaResourceResolver();

    @Ignore("Not compatible with API change in sonar 4.2")
    @Test
    public void type() {
    	//FIXME
        File javaFile = (File) resourceResolver.resolve("Type", JavaResourceResolverTest.class.getName());
        assertThat(javaFile.getLongName(), equalTo(JavaResourceResolverTest.class.getName()));
    }

}
