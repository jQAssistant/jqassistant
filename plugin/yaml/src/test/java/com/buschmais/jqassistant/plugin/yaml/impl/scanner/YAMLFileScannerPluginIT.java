package com.buschmais.jqassistant.plugin.yaml.impl.scanner;

import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.Finders.findKeyByName;
import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.Finders.findValueByValue;
import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.util.StringValueMatcher.hasValue;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.io.File;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLKeyDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLValueDescriptor;

public class YAMLFileScannerPluginIT extends AbstractPluginIT {

    @Before
    public void startTransaction() {
        store.beginTransaction();
    }

    @After
    public void commitTransaction() {
        store.commitTransaction();
    }


    @Test
    public void scanReturnsFileDescriptorWithCorrectFileName() {
        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/valid/simple-key-value-pair.yaml");

        Scanner scanner = getScanner();
        YAMLFileDescriptor file = scanner.scan(yamlFile, yamlFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/simple-key-value-pair.yaml"));
    }


    @Test
    public void scanSimpleKeyValuePairYAML() {
        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/valid/simple-key-value-pair.yaml");

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query("MATCH (f:YAML:File) WHERE f.fileName=~'.*/probes/valid/simple-key-value-pair.yaml' RETURN f")
                .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));

        YAMLFileDescriptor file = fileDescriptors.get(0);
        assertThat(file.getDocuments(), hasSize(1));

        YAMLDocumentDescriptor document = file.getDocuments().get(0);

        assertThat(document.getValues(), hasSize(0));
        assertThat(document.getKeys(), hasSize(1));

        YAMLKeyDescriptor key = findKeyByName(document.getKeys(), "key");

        assertThat(key.getName(), equalTo("key"));
        assertThat(key.getFullQualifiedName(), equalTo("key"));
        assertThat(key.getValues(), hasSize(1));
        assertThat(key.getPosition(), equalTo(0));

        YAMLValueDescriptor value = findValueByValue(key.getValues(), "value");

        assertThat(value.getValue(), equalTo("value"));
        assertThat(value.getPosition(), equalTo(0));
    }


    @Test
    public void scanTwoSimpleKeyValuePairsYAML() {
        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/valid/two-simple-key-value-pairs.yaml");


        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query("MATCH (f:YAML:File) WHERE f.fileName=~'.*/two-simple-key-value-pairs.yaml' RETURN f")
                  .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));

        YAMLFileDescriptor file = fileDescriptors.get(0);
        assertThat(file.getDocuments(), hasSize(1));

        YAMLDocumentDescriptor document = file.getDocuments().get(0);

        assertThat(document.getValues(), hasSize(0));
        assertThat(document.getKeys(), hasSize(2));

        YAMLKeyDescriptor keyA = findKeyByName(document.getKeys(), "keyA");

        assertThat(keyA.getName(), equalTo("keyA"));
        assertThat(keyA.getPosition(), equalTo(0));
        assertThat(keyA.getFullQualifiedName(), equalTo("keyA"));
        assertThat(keyA.getValues(), hasSize(1));

        YAMLValueDescriptor valueOfKeyA = findValueByValue(keyA.getValues(), "valueA");

        assertThat(valueOfKeyA.getValue(), equalTo("valueA"));
        assertThat(valueOfKeyA.getPosition(), equalTo(0));
    }


    @Test
    public void scanSimpleListYAML() {
        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/valid/simple-list.yaml");

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query("MATCH (f:YAML:File) WHERE f.fileName=~'.*/simple-list.yaml' RETURN f").getColumn("f");

        assertThat(fileDescriptors, hasSize(1));
        assertThat(fileDescriptors.get(0).getDocuments(), hasSize(1));

        YAMLDocumentDescriptor documentDescriptor = fileDescriptors.get(0).getDocuments().get(0);

        assertThat(documentDescriptor.getKeys(), hasSize(1));
        assertThat(documentDescriptor.getValues(), empty());

        YAMLKeyDescriptor keyDescriptor = documentDescriptor.getKeys().get(0);

        assertThat(keyDescriptor.getName(), equalTo("alist"));
        assertThat(keyDescriptor.getValues(), hasSize(3));
        assertThat(keyDescriptor.getKeys(), empty());
        assertThat(keyDescriptor.getValues(), containsInAnyOrder(hasValue("a"), hasValue("b"),
                                                                 hasValue("c")));
    }

    @Test
    public void scanSequenceOfScalarsYAML() {
        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/yamlspec/1.1/sec-2.1-example-2.1-sequence-of-scalars.yaml");

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query("MATCH (f:YAML:File) WHERE f.fileName=~'.*/sec-2.1-example-2.1-sequence-of-scalars.yaml' RETURN f")
                  .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));
        List<YAMLDocumentDescriptor> documents = fileDescriptors.get(0).getDocuments();

        assertThat(documents, hasSize(1));

        YAMLDocumentDescriptor document = documents.get(0);

        assertThat(document.getKeys(), empty());
        assertThat(document.getValues(), hasSize(3));
        assertThat(document.getValues(), containsInAnyOrder(hasValue("Mark McGwire"),
                                                            hasValue("Sammy Sosa"),
                                                            hasValue("Ken Griffey")));
    }

    @Test
    public void scanScalarsOfScalarsYAML() {
        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/yamlspec/1.1/sec-2.1-example-2.2-scalars-of-scalars.yaml");

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query("MATCH (f:YAML:File) WHERE f.fileName=~'.*/sec-2.1-example-2.2-scalars-of-scalars.yaml' RETURN f")
                  .getColumn("f");

        assertThat(fileDescriptors, not(empty()));
        assertThat(fileDescriptors, hasSize(1));

        YAMLFileDescriptor yamlFileDescriptor = fileDescriptors.get(0);

        assertThat(yamlFileDescriptor.getDocuments(), hasSize(1));

        YAMLDocumentDescriptor yamlDocumentDescriptor = yamlFileDescriptor.getDocuments().get(0);

        assertThat(yamlDocumentDescriptor.getKeys(), hasSize(3));
        assertThat(yamlDocumentDescriptor.getValues(), Matchers.empty());

        assertThat(findKeyByName(yamlDocumentDescriptor.getKeys(), "hr"), Matchers.notNullValue());
        assertThat(findKeyByName(yamlDocumentDescriptor.getKeys(), "avg"), Matchers.notNullValue());
        assertThat(findKeyByName(yamlDocumentDescriptor.getKeys(), "rbi"), Matchers.notNullValue());

        assertThat(findKeyByName(yamlDocumentDescriptor.getKeys(), "hr").getValues(), hasSize(1));
        assertThat(findKeyByName(yamlDocumentDescriptor.getKeys(), "avg").getValues(), hasSize(1));
        assertThat(findKeyByName(yamlDocumentDescriptor.getKeys(), "rbi").getValues(), hasSize(1));
    }

    @Test
    public void scanValidDropWizardConfigYAML() {
        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/valid/dropwizard-configuration.yaml");

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query("MATCH (f:YAML:File) WHERE f.fileName=~'.*/dropwizard-configuration.yaml' RETURN f")
                  .getColumn("f");

        YAMLFileDescriptor fileDescriptor = fileDescriptors.get(0);

        assertThat(fileDescriptor.getDocuments(), hasSize(1));

        YAMLDocumentDescriptor documentDescriptor = fileDescriptor.getDocuments().get(0);

        assertThat(documentDescriptor.getValues(), Matchers.empty());
        assertThat(documentDescriptor.getKeys(), hasSize(2));

        YAMLKeyDescriptor keyDescriptor = findKeyByName(documentDescriptor.getKeys(), "server");

        assertThat(keyDescriptor.getName(), equalTo("server"));
        assertThat(keyDescriptor.getFullQualifiedName(), equalTo("server"));

        assertThat(keyDescriptor.getKeys(), hasSize(4));

        YAMLKeyDescriptor subKey1 = findKeyByName(keyDescriptor.getKeys(), "maxThreads");
        YAMLKeyDescriptor subKey2 = findKeyByName(keyDescriptor.getKeys(), "applicationConnectors");
        YAMLKeyDescriptor subKey3 = findKeyByName(keyDescriptor.getKeys(), "adminConnectors");
        YAMLKeyDescriptor subKey4 = findKeyByName(keyDescriptor.getKeys(), "requestLog");

        assertThat(subKey1.getName(), equalTo("maxThreads"));
        assertThat(subKey2.getName(), equalTo("applicationConnectors"));
        assertThat(subKey3.getName(), equalTo("adminConnectors"));
        assertThat(subKey4.getName(), equalTo("requestLog"));
    }

    @Test
    public void scanMappingScalarsToSequencesYAML() {
        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/yamlspec/1.1/sec-2.1-example-2.3-mapping-scalars-to-sequences.yaml");

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query("MATCH (f:YAML:File) WHERE f.fileName=~'.*/sec-2.1-example-2.3-mapping-scalars-to-sequences.yaml' RETURN f")
                  .getColumn("f");

        YAMLFileDescriptor fileDescriptor = fileDescriptors.get(0);

        assertThat(fileDescriptor.getDocuments(), hasSize(1));

        YAMLDocumentDescriptor docDescriptor = fileDescriptor.getDocuments().get(0);


        YAMLKeyDescriptor keyDescriptor1 = findKeyByName(docDescriptor.getKeys(), "american");

        assertThat(keyDescriptor1.getName(), equalTo("american"));
        assertThat(keyDescriptor1.getFullQualifiedName(), equalTo("american"));
        assertThat(keyDescriptor1.getValues(), hasSize(3));
        assertThat(keyDescriptor1.getKeys(), empty());
        assertThat(keyDescriptor1.getValues(), containsInAnyOrder(hasValue("Boston Red Sox"),
                                                                  hasValue("New York Yankees"),
                                                                  hasValue("Detroit Tigers")));

        YAMLKeyDescriptor keyDescriptor2 = findKeyByName(docDescriptor.getKeys(), "national");

        assertThat(keyDescriptor2.getName(), equalTo("national"));
        assertThat(keyDescriptor2.getFullQualifiedName(), equalTo("national"));
        assertThat(keyDescriptor2.getValues(), hasSize(3));
        assertThat(keyDescriptor2.getKeys(), empty());
        assertThat(keyDescriptor2.getValues(), containsInAnyOrder(hasValue("New York Mets"),
                                                                  hasValue("Chicago Cubs"),
                                                                  hasValue("Atlanta Braves")));
    }

    @Test
    @Ignore
    public void scanSequenceOfMappingsYAML() {
        Assert.fail("Not implemented yet!");
//             {"/probes/yamlspec/1.1/sec-2.1-example-2.4-sequence-of-mappings.yaml"},
    }

    @Test
    public void scanSequenceOfSequencesYAML() {
        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/yamlspec/1.1/sec-2.1-example-2.5-sequence-of-sequences.yaml");

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors = query("MATCH (f:YAML:File) WHERE f.fileName=~'.*/1.1/sec-2.1-example-2.5-sequence-of-sequences.yaml' RETURN f")
             .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));

        YAMLFileDescriptor file = fileDescriptors.get(0);

        assertThat(file.getDocuments(), hasSize(1));

        YAMLDocumentDescriptor document = file.getDocuments().get(0);

        assertThat(document.getKeys(), empty());
        assertThat(document.getValues(), hasSize(3));

        YAMLValueDescriptor firstSequence = document.getValues().get(0);
        YAMLValueDescriptor secondSequence = document.getValues().get(1);
        YAMLValueDescriptor thirdSequence = document.getValues().get(2);

        assertThat(firstSequence.getValue(), nullValue());
        assertThat(firstSequence.getValues(), hasSize(3));
        assertThat(firstSequence.getValues(), containsInAnyOrder(hasValue("name"), hasValue("hr"),
                                                                 hasValue("avg")));

        assertThat(secondSequence.getValue(), nullValue());
        assertThat(secondSequence.getValues(), hasSize(3));

        assertThat(thirdSequence.getValue(), CoreMatchers.nullValue());
        assertThat(thirdSequence.getValues(), containsInAnyOrder(hasValue("Sammy Sosa"),
                                                                 hasValue("63"),
                                                                 hasValue("0.288")));
    }

    @Test
    public void scanMappingOfMappingsYAML() {
        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/yamlspec/1.1/sec-2.1-example-2.6-mapping-of-mappings.yaml");

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query("MATCH (f:YAML:File) WHERE f.fileName=~'.*/1.1/sec-2.1-example-2.6-mapping-of-mappings.yaml' RETURN f")
                  .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));

        YAMLFileDescriptor fileDescriptor = fileDescriptors.get(0);

        assertThat(fileDescriptor.getDocuments(), hasSize(1));

        YAMLDocumentDescriptor document = fileDescriptor.getDocuments().get(0);

        assertThat(document.getKeys(), hasSize(2));
        assertThat(document.getValues(), empty());

        YAMLKeyDescriptor keyA = findKeyByName(document.getKeys(), "Mark McGwire");

        assertThat(keyA.getKeys(), hasSize(2));
        assertThat(keyA.getName(), equalTo("Mark McGwire"));
        assertThat(keyA.getValues(), empty());

        YAMLKeyDescriptor keyA1 = findKeyByName(keyA.getKeys(), "hr");


        System.out.println(keyA1.getName());
        System.out.println(keyA1.getFullQualifiedName());

        assertThat(keyA1.getName(), equalTo("hr"));
        assertThat(keyA1.getFullQualifiedName(), CoreMatchers.equalTo("Mark McGwire.hr"));
        assertThat(keyA1.getKeys(), empty());
        assertThat(keyA1.getValues(), hasSize(1));
        assertThat(keyA1.getValues(), contains(hasValue("65")));


        YAMLKeyDescriptor keyA2 = findKeyByName(keyA.getKeys(), "avg");

        System.out.println(keyA2.getName());
        System.out.println(keyA2.getFullQualifiedName());

        assertThat(keyA2.getName(), equalTo("avg"));
        assertThat(keyA2.getFullQualifiedName(), CoreMatchers.equalTo("Mark McGwire.avg"));
        assertThat(keyA2.getKeys(), empty());
        assertThat(keyA2.getValues(), hasSize(1));
        assertThat(keyA2.getValues(), contains(hasValue("0.278")));

        //---

        YAMLKeyDescriptor keyB = findKeyByName(document.getKeys(), "Sammy Sosa");

        assertThat(keyB.getName(), CoreMatchers.equalTo("Sammy Sosa"));
        assertThat(keyB.getValues(), empty());
        assertThat(keyB.getKeys(), hasSize(2));

        YAMLKeyDescriptor keyB2 = findKeyByName(keyB.getKeys(), "avg");

        assertThat(keyB2.getFullQualifiedName(), CoreMatchers.equalTo("Sammy Sosa.avg"));
        assertThat(keyB2.getValues(), hasSize(1));
        assertThat(keyB2.getValues(), hasItem(hasValue("0.288")));
    }

    @Test
    @Ignore
    public void scanlayByPlayYAML() {
        Assert.fail("Not implemented yet!");
//             {"/probes/yamlspec/1.1/sec-2.2-example-2.8-play-by-play.yaml"},
    }

    @Test
    @Ignore
    public void scanTwoDocumenstsInAStreamYAML() {
        Assert.fail("Not implemented yet!");
//             {"/probes/yamlspec/1.1/sec-2.2-example-2.7-two-documensts-in-a-stream.yaml"},
    }

    @Test
    public void scanSingleDocumentWithCommentsYAML() {
        String fileName = "sec-2.2-example-2.9-single-document-with-comments.yaml";

        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/yamlspec/1.1/" + fileName);

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query(format("MATCH (f:YAML:File) WHERE f.fileName=~'.*/1.1/%s' RETURN f", fileName))
                  .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));
        YAMLFileDescriptor fileDescriptor = fileDescriptors.get(0);

        assertThat(fileDescriptor.getDocuments(), hasSize(1));

        YAMLDocumentDescriptor document = fileDescriptor.getDocuments().get(0);

        assertThat(document.getKeys(), hasSize(2));
        assertThat(document.getValues(), empty());

        // Enough tests. The same structure is covered by other tests
    }

    @Test
    public void scanNodeForSammySosaTwice() {
        String fileName = "sec-2.2-example-2.10-node-for-sammy-sosa-twice.yaml";
        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/yamlspec/1.1/" + fileName);

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query(format("MATCH (f:YAML:File) WHERE f.fileName=~'.*/1.1/%s' RETURN f", fileName))
                  .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));

        YAMLFileDescriptor fileDescriptor = fileDescriptors.get(0);

        assertThat(fileDescriptor.getDocuments(), hasSize(1));

        YAMLDocumentDescriptor document = fileDescriptor.getDocuments().get(0);

        assertThat(document.getKeys(), hasSize(2));
        assertThat(document.getValues(), empty());

        //-- In this test I will try to find the nodes by its fqn

        List<YAMLKeyDescriptor> nodes = query("MATCH (k:YAML:Key) RETURN k").getColumn("k");

        assertThat(nodes, hasSize(2));

        YAMLKeyDescriptor rbiNode = findKeyByName(nodes, "rbi");

        assertThat(rbiNode, Matchers.notNullValue());
        assertThat(rbiNode.getValues(), hasSize(2));

        for (YAMLValueDescriptor valueDescriptor : rbiNode.getValues()) {
            System.out.println(valueDescriptor.getValue());
        }

        assertThat(rbiNode.getValues(), hasItem(hasValue("Sammy Sosa")));
    }

//    @Test
//    public void scan//             {"/probes/yamlspec/1.1/sec-2.2-example-2.11-mapping-betweend-sequences.yaml"},
//             {"/probes/yamlspec/1.1/sec-2.2-example-2.11-mapping-betweend-sequences.yaml"},

//    @Test
//    public void scan//             {"/probes/yamlspec/1.1/sec-2.2-example-2.12-in-line-nested-mapping.yaml"},
//             {"/probes/yamlspec/1.1/sec-2.2-example-2.12-in-line-nested-mapping.yaml"},

//    @Test
//    public void scan//             {"/probes/yamlspec/1.1/sec-2.3-example-2.13-in-literals-newlines-preserved.yaml"},
//             {"/probes/yamlspec/1.1/sec-2.3-example-2.13-in-literals-newlines-preserved.yaml"},

//    @Test
//    public void scan//             {"/probes/yamlspec/1.1/sec-2.3-example-2.14-in-the-plain-scalar-newline-as-spaces.yaml"},
//             {"/probes/yamlspec/1.1/sec-2.3-example-2.14-in-the-plain-scalar-newline-as-spaces.yaml"},

    @Test
    public void scanFoldedNewLinesArePreserved() {
        String fileName = "sec-2.3-example-2.15-folded-newlines-are-preserved.yaml";

        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/yamlspec/1.1/" + fileName);

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query(format("MATCH (f:YAML:File) WHERE f.fileName=~'.*/1.1/%s' RETURN f", fileName))
                  .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));

        YAMLFileDescriptor fileDescriptor = fileDescriptors.get(0);

        assertThat(fileDescriptor.getDocuments(), hasSize(1));

        YAMLDocumentDescriptor document = fileDescriptor.getDocuments().get(0);

        assertThat(document.getKeys(), empty());
        assertThat(document.getValues(), hasSize(1));

        YAMLValueDescriptor valueDescriptor = document.getValues().get(0);

        assertThat(valueDescriptor.getValue(), equalTo("Sammy Sosa completed another" +
                                                            " fine season with great stats.\n" +
                                                            "\n" +
                                                            "  63 Home Runs\n" +
                                                            "  0.288 Batting Average\n" +
                                                            "\n" +
                                                            "What a year!"));

        List<YAMLValueDescriptor> valuesOfDocument =
             query(format("MATCH (f:YAML:File)-[:CONTAINS_DOCUMENT]->(d:YAML:Document)-[:CONTAINS_VALUE]->(v) WHERE f.fileName=~'.*/1.1/%s' RETURN v", fileName))
                  .getColumn("v");

        assertThat(valuesOfDocument, hasSize(1));

        YAMLValueDescriptor valueOfDocument = valuesOfDocument.get(0);

        assertThat(valueOfDocument.getValue(), equalTo("Sammy Sosa completed another" +
                                                       " fine season with great stats.\n" +
                                                       "\n" +
                                                       "  63 Home Runs\n" +
                                                       "  0.288 Batting Average\n" +
                                                       "\n" +
                                                       "What a year!"));
    }

    @Test
    @Ignore
    public void scanIndentationDeterminesScopeYAML() {
//             {"/probes/yamlspec/1.1/sec-2.3-example-2.16-indentation-determines-scope.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    @Ignore
    public void scanQuotedScalarsYAML() {
//             {"/probes/yamlspec/1.1/sec-2.3-example-2.17-quoted-scalars.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    @Ignore
    public void scanMultiLineFlowScalarsYAML() {
//             {"/probes/yamlspec/1.1/sec-2.3-example-2.18-multi-line-flow-scalars.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    @Ignore
    public void scanIntegersYAML() {
//             {"/probes/yamlspec/1.1/sec-2.4-example-2.19-integers.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    @Ignore
    public void scanFloatingPointYAML() {
//             {"/probes/yamlspec/1.1/sec-2.4-example-2.20-floating-point.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    @Ignore
    public void scanMiscYAML() {
//             {"/probes/yamlspec/1.1/sec-2.4-example-2.21-misc.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    @Ignore
    public void scanTimestampsYAML() {
//             {"/probes/yamlspec/1.1/sec-2.4-example-2.22-timestamps.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    @Ignore
    public void scanVariousExplicitTagsYAML() {
//             {"/probes/yamlspec/1.1/sec-2.4-example-2.23-various-explicit-tags.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test @Ignore
    public void scanGlobalTagsYAML() {
//             {"/probes/yamlspec/1.1/sec-2.4-example-2.24-global-tags.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    @Ignore
    public void scanUnorderedSetsYAML() {
//             {"/probes/yamlspec/1.1/sec-2.4-example-2.25-unordered-sets.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    @Ignore
    public void scanOrderedMappingsYAML() {
//             {"/probes/yamlspec/1.1/sec-2.4-example-2.26-ordered-mappings.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    @Ignore
    public void scanExampleInvoiceYAML() {
//             {"/probes/yamlspec/1.1/sec-2.5-example-2.27-invoice.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    @Ignore
    public void scanLogFileYAML() {
//             {"/probes/yamlspec/1.1/sec-2.5-example-2.28-log-file.yaml"},
        Assert.fail("Not implemented yet!");
    }

    @Test
    public void scanAnInvalidMappingAndParsedIsFalse() {
        String fileName = "invalid-mapping.yaml";

        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/invalid/" + fileName);

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query(format("MATCH (f:YAML:File) WHERE f.fileName=~'.*/%s' RETURN f", fileName))
                  .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));

        YAMLFileDescriptor fileDescriptor = fileDescriptors.get(0);

        assertThat(fileDescriptor.isInvalid(), is(true));
    }

    @Test
    public void scanAnValidMappingAndParsedIsTrue() {
        String fileName = "simple-list.yaml";

        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/valid/" + fileName);

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query(format("MATCH (f:YAML:File) WHERE f.fileName=~'.*/%s' RETURN f", fileName))
                  .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));

        YAMLFileDescriptor fileDescriptor = fileDescriptors.get(0);

        assertThat(fileDescriptor.isInvalid(), is(false));
    }

    @Test
    public void invalidDocumentInHostConfigInvalidLeedsToParsingError() {
        String fileName = "hostconfig-invalid.yaml";

        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/invalid/" + fileName);

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query(format("MATCH (f:YAML:File) WHERE f.fileName=~'.*/%s' RETURN f", fileName))
                  .getColumn("f");

        YAMLFileDescriptor fileDescriptor = fileDescriptors.get(0);

        assertThat(fileDescriptor.isInvalid(), is(true));
    }

    @Test
    public void ifParsingFailsThereWillBeNoNodesForTheContentOfTheYAMLFile() {
        String fileName = "hostconfig-invalid.yaml";

        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/invalid/" + fileName);

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query(format("MATCH (f:YAML:File) WHERE f.fileName=~'.*/%s' RETURN f", fileName))
                  .getColumn("f");

        YAMLFileDescriptor fileDescriptor = fileDescriptors.get(0);

        assertThat(fileDescriptor.isInvalid(), is(true));

        List<YAMLFileDescriptor> childNodes =
             query(format("MATCH (f:YAML:File)-[*]->(c) WHERE f.fileName=~'.*/%s' RETURN c", fileName))
                  .getColumn("c");

        assertThat(childNodes, anyOf(empty(), nullValue()));
    }

    @Test
    public void ifParsingFailsThePropertyInvalidWillBeTrue() {
        String fileName = "hostconfig-invalid.yaml";

        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/invalid/" + fileName);

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query(format("MATCH (f:YAML:File) WHERE f.fileName=~'.*/%s' AND " +
                          "f.invalid = true RETURN f", fileName))
                  .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));
    }

    @Test
    public void ifParsingSuccedsThePropertyInvalidWillBeFalse() {
        String fileName = "simple-list.yaml";

        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/valid/" + fileName);

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query(format("MATCH (f:YAML:File) WHERE f.fileName=~'.*/%s' AND " +
                          "f.invalid = false RETURN f", fileName))
                  .getColumn("f");

        assertThat(fileDescriptors, hasSize(1));
    }

    @Test
    public void ifParsingFailsThereWillBeNoNodesForTheContentOfTheSecondYAMLDocument() {
        String fileName = "hostconfig-2-invalid.yaml";

        File yamlFile = new File(getClassesDirectory(YAMLFileScannerPluginValidFileSetIT.class),
                                 "/probes/invalid/" + fileName);

        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        List<YAMLFileDescriptor> fileDescriptors =
             query(format("MATCH (f:YAML:File) WHERE f.fileName=~'.*/%s' RETURN f", fileName))
                  .getColumn("f");

        YAMLFileDescriptor fileDescriptor = fileDescriptors.get(0);

        assertThat(fileDescriptor.isInvalid(), is(true));

        List<YAMLFileDescriptor> childNodes =
             query(format("MATCH (f:YAML:File)-[*]->(c) WHERE f.fileName=~'.*/%s' RETURN c", fileName))
                  .getColumn("c");

        assertThat(childNodes, anyOf(empty(), nullValue()));
    }
}