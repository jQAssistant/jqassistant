package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaRuntimePT extends AbstractPluginIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaRuntimePT.class);

    /**
     * The list of primitive types.
     */
    public static final Class<?>[] PRIMITIVE_TYPES = new Class<?>[] { void.class, boolean.class, short.class, int.class, float.class, double.class, long.class };

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
        Iterator<FileDescriptor> iterator = getFileScanner().scanArchive(runtimeJar).iterator();
        Descriptor descriptor;
        do {
            int count = 0;
            store.beginTransaction();
            do {
                if (iterator.hasNext()) {
                    descriptor = iterator.next();
                    count++;
                } else {
                    descriptor = null;
                }
            } while (descriptor != null && count < 50);
            store.commitTransaction();
        } while (descriptor != null);
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
        for (Result<Concept> conceptResult : reportWriter.getConceptResults()) {
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
