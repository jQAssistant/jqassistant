package com.buschmais.jqassistant.rules.java.test.dependency;

import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.report.api.ReportWriterException;
import com.buschmais.jqassistant.scanner.test.set.pojo.Pojo;
import com.buschmais.jqassistant.store.api.QueryResult;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.buschmais.jqassistant.scanner.test.matcher.ClassDescriptorMatcher.classDescriptor;
import static org.junit.Assert.assertThat;

/**
 * Tests for the concept java:AssignableFrom.
 */
public class AssignableFromIT extends AbstractAnalysisIT {

    @Test
    public void assignableFrom() throws IOException, ReportWriterException {
        scanClasses(Pojo.class);
        applyConcept("java:AssignableFrom");
        QueryResult result = store.executeQuery("MATCH (pojo:CLASS)<-[:ASSIGNABLE_FROM]-(c) RETURN c");
        List<ClassDescriptor> descriptors = new ArrayList<ClassDescriptor>();
        for (QueryResult.Row row : result.getRows()) {
            ClassDescriptor c = row.get("c");
            descriptors.add(c);
        }
        Matcher<Iterable<ClassDescriptor>> matcher = CoreMatchers.hasItems(classDescriptor(Pojo.class), classDescriptor(Object.class));
        assertThat(descriptors, matcher);
    }
}
