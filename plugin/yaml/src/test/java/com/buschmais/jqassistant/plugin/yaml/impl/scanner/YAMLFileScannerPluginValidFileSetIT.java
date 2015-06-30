package com.buschmais.jqassistant.plugin.yaml.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLFileDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class YAMLFileScannerPluginValidFileSetIT extends AbstractPluginIT {

    private String pathToYAMLFile;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
             {"/probes/valid/simple-key-value-pair.yaml"},
             {"/probes/valid/two-simple-key-value-pairs.yaml"},
             {"/probes/valid/simple-list.yaml"},
             {"/probes/valid/dropwizard-configuration.yaml"},
//             {"/probes/yamlspec/1.1/sec-2.1-example-2.1-sequence-of-scalars.yaml"},
             {"/probes/yamlspec/1.1/sec-2.1-example-2.2-scalars-of-scalars.yaml"},
             {"/probes/yamlspec/1.1/sec-2.1-example-2.3-mapping-scalars-to-sequences.yaml"},
//             {"/probes/yamlspec/1.1/sec-2.1-example-2.4-sequence-of-mappings.yaml"},
//             {"/probes/yamlspec/1.1/sec-2.1-example-2.5-sequence-of-sequences.yaml"},
             {"/probes/yamlspec/1.1/sec-2.1-example-2.6-mapping-of-mappings.yaml"},
             {"/probes/yamlspec/1.1/sec-2.2-example-2.8-play-by-play.yaml"},
//             {"/probes/yamlspec/1.1/sec-2.2-example-2.7-two-documensts-in-a-stream.yaml"},
             {"/probes/yamlspec/1.1/sec-2.2-example-2.9-single-document-with-comments.yaml"},
             {"/probes/yamlspec/1.1/sec-2.2-example-2.10-node-for-sammy-sosa-twice.yaml"},
              // @todo  {"/probes/yamlspec/1.1/sec-2.2-example-2.11-mapping-betweend-sequences.yaml"},
//             {"/probes/yamlspec/1.1/sec-2.2-example-2.12-in-line-nested-mapping.yaml"},
             {"/probes/yamlspec/1.1/sec-2.3-example-2.13-in-literals-newlines-preserved.yaml"},
             {"/probes/yamlspec/1.1/sec-2.3-example-2.14-in-the-plain-scalar-newline-as-spaces.yaml"},
             {"/probes/yamlspec/1.1/sec-2.3-example-2.15-folded-newlines-are-preserved.yaml"},
             {"/probes/yamlspec/1.1/sec-2.3-example-2.16-indentation-determines-scope.yaml"},
             {"/probes/yamlspec/1.1/sec-2.3-example-2.17-quoted-scalars.yaml"},
             {"/probes/yamlspec/1.1/sec-2.3-example-2.18-multi-line-flow-scalars.yaml"},
             {"/probes/yamlspec/1.1/sec-2.4-example-2.19-integers.yaml"},
             {"/probes/yamlspec/1.1/sec-2.4-example-2.20-floating-point.yaml"},
             {"/probes/yamlspec/1.1/sec-2.4-example-2.21-misc.yaml"},
             {"/probes/yamlspec/1.1/sec-2.4-example-2.22-timestamps.yaml"},
             {"/probes/yamlspec/1.1/sec-2.4-example-2.23-various-explicit-tags.yaml"},
             // @todo {"/probes/yamlspec/1.1/sec-2.4-example-2.24-global-tags.yaml"},
             {"/probes/yamlspec/1.1/sec-2.4-example-2.25-unordered-sets.yaml"},
             {"/probes/yamlspec/1.1/sec-2.4-example-2.26-ordered-mappings.yaml"},
             // @todo {"/probes/yamlspec/1.1/sec-2.5-example-2.27-invoice.yaml"},
             {"/probes/yamlspec/1.1/sec-2.5-example-2.28-log-file.yaml"},
        });
    }

    public YAMLFileScannerPluginValidFileSetIT(String file) {
        pathToYAMLFile = file;
    }

    @Test
    public void canLoadYAMLFile() {
        store.beginTransaction();

        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class), pathToYAMLFile);

        Scanner scanner = getScanner();
        YAMLFileDescriptor descriptor = scanner.scan(yamlFile, yamlFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   descriptor, notNullValue());

        store.commitTransaction();
    }

}