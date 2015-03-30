package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.a.AnnotationType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.a.ClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.a.EnumType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.a.ExceptionType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.b.DependentType;

/**
 * Tests for the dependency concepts and result.
 */
public class ClasspathIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "classpath:resolveType".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveType() throws IOException, AnalysisException {
        scanAndApply("classpath:ResolveType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("a", "b").get();
        List<Map<String, Object>> rows = query("MATCH (a:Artifact)-[:REQUIRES]->(t1:Type)-[:RESOLVES_TO]->(t2:Type) WHERE a.fqn={a} RETURN t1, t2", params)
                .getRows();
        assertThat(rows.size(), equalTo(4));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveDependency".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveDependency() throws IOException, AnalysisException {
        scanAndApply("classpath:ResolveType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).get();
        List<TypeDescriptor> dependentTypes = query("MATCH (dependentTypes:Type) WHERE dependentTypes.fqn={dependentType} RETURN dependentTypes", params)
                .getColumn("dependentTypes");
        assertThat(dependentTypes.size(), equalTo(1));
        TypeDescriptor dependentType = dependentTypes.get(0);
        store.commitTransaction();
    }

    private void scanAndApply(String concept) throws IOException, AnalysisException {
        scanClasses("a", ClassType.class, AnnotationType.class, EnumType.class, ExceptionType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept(concept).getStatus(), equalTo(SUCCESS));
    }
}
