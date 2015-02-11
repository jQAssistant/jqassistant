package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaRuntimePT extends AbstractJavaPluginIT {

    /**
     * The list of primitive types.
     */
    public static final Class<?>[] PRIMITIVE_TYPES = new Class<?>[] { void.class, boolean.class, short.class, int.class, float.class, double.class, long.class };
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaRuntimePT.class);

    /**
     * Scans the rt.jar of the Java Runtime Environment specified by the
     * environment variable java.home.
     * 
     * @throws IOException
     *             If scanning fails.
     */
    @Test
    public void javaRuntime01Scan() throws IOException, AnalysisException {
        String javaHome = System.getProperty("java.home");
        Assume.assumeNotNull("java.home is not set.", javaHome);
        File runtimeJar = new File(javaHome + "/lib/rt.jar");
        Assume.assumeTrue("Java Runtime JAR not found: " + runtimeJar.getAbsolutePath(), runtimeJar.exists());
        store.beginTransaction();
        getScanner().scan(runtimeJar, runtimeJar.getAbsolutePath(), null);
        store.commitTransaction();
    }

    @Test
    @TestStore(reset = false)
    public void javaRuntime02Analyze() throws IOException, AnalysisException {
        applyConcept("metric:Top10TypesPerArtifact");
        applyConcept("metric:Top10TypesPerPackage");
        applyConcept("metric:Top10MethodsPerType");
        applyConcept("metric:Top10FieldsPerType");
        applyConcept("metric:Top10TypeFanIn");
        applyConcept("metric:Top10TypeFanOut");
        for (Result<Concept> conceptResult : reportWriter.getConceptResults().values()) {
            LOGGER.info(conceptResult.getRule().getId());
            for (Map<String, Object> row : conceptResult.getRows()) {
                StringBuffer sb = new StringBuffer("\t");
                for (Object value : row.values()) {
                    sb.append(value);
                    sb.append("\t");
                }
                LOGGER.info(sb.toString());
            }
        }
    }
}
