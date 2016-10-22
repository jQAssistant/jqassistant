package com.buschmais.jqassistant.plugin.json.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugins.json.api.model.JSONArrayDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONDocumentDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONFileDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONKeyDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONObjectDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONScalarValueDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONValueDescriptor;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;

public class JSONFileScannerPluginIT extends AbstractPluginIT {

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
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/true-false-null.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/true-false-null.json"));
    }

    @Test
    public void scanReturnsObjectWithOneKeyValuePair() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/object-one-key-value-pair.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/object-one-key-value-pair.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), CoreMatchers.equalTo("A"));
        assertThat(keyDescriptor.getValue(), notNullValue());
        assertThat(keyDescriptor.getValue().getValue(), IsEqual.<Object>equalTo("B"));
    }

    @Test
    public void scanReturnsObjectWithTwoKeyValuePairs() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/object-two-key-value-pairs.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/object-two-key-value-pairs.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(2));

        JSONKeyDescriptor keyDescriptorA = findKeyInDocument(jsonObject.getKeys(), "A");
        JSONKeyDescriptor keyDescriptorB = findKeyInDocument(jsonObject.getKeys(), "C");

        assertThat(keyDescriptorA.getName(), CoreMatchers.equalTo("A"));
        assertThat(keyDescriptorA.getValue(), Matchers.notNullValue());
        assertThat(keyDescriptorA.getValue().getValue(), Matchers.<Object>equalTo("B"));
        assertThat(keyDescriptorB.getName(), CoreMatchers.equalTo("C"));
        assertThat(keyDescriptorB.getValue().getValue(), Matchers.<Object>equalTo("D"));
    }

    @Test
    public void scanReturnsObjectWithTrueFalseAndNullValue() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/true-false-null.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/true-false-null.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(3));

        JSONKeyDescriptor keyDescriptorA = findKeyInDocument(jsonObject.getKeys(), "A");
        JSONKeyDescriptor keyDescriptorB = findKeyInDocument(jsonObject.getKeys(), "B");
        JSONKeyDescriptor keyDescriptorC = findKeyInDocument(jsonObject.getKeys(), "C");

        assertThat(keyDescriptorA.getName(), CoreMatchers.equalTo("A"));
        assertThat(keyDescriptorA.getValue(), Matchers.notNullValue());
        assertThat(keyDescriptorA.getValue().getValue(), Matchers.<Object>equalTo(Boolean.TRUE));

        assertThat(keyDescriptorB.getName(), CoreMatchers.equalTo("B"));
        assertThat(keyDescriptorB.getValue(), Matchers.notNullValue());
        assertThat(keyDescriptorB.getValue().getValue(), Matchers.<Object>equalTo(Boolean.FALSE));

        assertThat(keyDescriptorC.getName(), CoreMatchers.equalTo("C"));
        assertThat(keyDescriptorC.getValue(), Matchers.nullValue());
    }

    @Test
    public void scanReturnsEmptyArray() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/array-empty.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/array-empty.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONArrayDescriptor jsonArray = (JSONArrayDescriptor) document.getContainer();

        assertThat(jsonArray.getValue(), Matchers.empty());
    }

    @Test
    public void scanReturnsArrayWithOneValue() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/array-one-value.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/array-one-value.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());
        assertThat(document.getContainer(), Matchers.instanceOf(JSONArrayDescriptor.class));

        JSONArrayDescriptor jsonArray = (JSONArrayDescriptor) document.getContainer();

        assertThat(jsonArray.getValue(), hasSize(1));

        JSONValueDescriptor<?> valueDescriptor = jsonArray.getValue().get(0);

        assertThat(valueDescriptor.getValue(), Matchers.<Object>equalTo("ABC"));
    }

    @Test
    public void scanReturnsObjectWithEmptyArray() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/object-with-array-empty.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/object-with-array-empty.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor)document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.<Object>equalTo("A"));
        assertThat(keyDescriptor.getValue(), Matchers.instanceOf(JSONArrayDescriptor.class));

        JSONArrayDescriptor arrayValueDescriptor = (JSONArrayDescriptor) keyDescriptor.getValue();

        assertThat(arrayValueDescriptor.getValue(), empty());
    }

    @Test
    public void scanReturnsObjectWithObject() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/object-with-object-empty.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/object-with-object-empty.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONValueDescriptor<?> value = keyDescriptor.getValue();

        assertThat(value, instanceOf(JSONObjectDescriptor.class));

        JSONObjectDescriptor objectDescriptor = (JSONObjectDescriptor) value;

        assertThat(objectDescriptor, Matchers.notNullValue());
        assertThat(objectDescriptor, Matchers.instanceOf(JSONObjectDescriptor.class));
    }

    @Test
    public void scannerCanHandleBlockCommentInObject() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/block-comment-in-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONValueDescriptor<?> value = keyDescriptor.getValue();

        assertThat(value, instanceOf(JSONScalarValueDescriptor.class));

        JSONScalarValueDescriptor scalarValueDescriptor = (JSONScalarValueDescriptor) value;

        Object object = scalarValueDescriptor.getValue();

        assertThat(object, Matchers.notNullValue());
        assertThat(object, Matchers.instanceOf(String.class));
        assertThat(object, Matchers.<Object>equalTo("B"));
    }

    @Test
    public void scannerCanHandleEmptyFile() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/empty-file.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());
        assertThat("Scanner must not return a document for an empty file.",
                   file.getDocument(), nullValue());
    }

    @Test
    public void parserHandlesLineCommentAfterObjectCorrectly() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/line-comment-after-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/line-comment-after-object.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONValueDescriptor<?> value = keyDescriptor.getValue();

        assertThat(value, instanceOf(JSONScalarValueDescriptor.class));

        JSONScalarValueDescriptor scalarValueDescriptor = (JSONScalarValueDescriptor) value;

        Object object = scalarValueDescriptor.getValue();

        assertThat(object, Matchers.notNullValue());
        assertThat(object, Matchers.instanceOf(String.class));
        assertThat(object, Matchers.<Object>equalTo("B"));
    }

    @Test
    public void parserHandlesLineCommentBeforeObjectCorrectly() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/line-comment-before-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/line-comment-before-object.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONValueDescriptor<?> value = keyDescriptor.getValue();

        assertThat(value, instanceOf(JSONScalarValueDescriptor.class));

        JSONScalarValueDescriptor scalarValueDescriptor = (JSONScalarValueDescriptor) value;

        Object object = scalarValueDescriptor.getValue();

        assertThat(object, Matchers.notNullValue());
        assertThat(object, Matchers.instanceOf(String.class));
        assertThat(object, Matchers.<Object>equalTo("B"));
    }

    @Test
    public void parserHandlesLineCommentInObjectCorrectly() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/line-comment-in-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/line-comment-in-object.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONValueDescriptor<?> value = keyDescriptor.getValue();

        assertThat(value, instanceOf(JSONScalarValueDescriptor.class));

        JSONScalarValueDescriptor scalarValueDescriptor = (JSONScalarValueDescriptor) value;

        Object object = scalarValueDescriptor.getValue();

        assertThat(object, Matchers.notNullValue());
        assertThat(object, Matchers.instanceOf(String.class));
        assertThat(object, Matchers.<Object>equalTo("B"));
    }

    @Test
    public void scanReturnsObjectWithArrayOfTwoElements() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/object-with-array-two-elements.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/object-with-array-two-elements.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONValueDescriptor<?> value = keyDescriptor.getValue();

        assertThat(value, instanceOf(JSONArrayDescriptor.class));

        JSONArrayDescriptor scalarValueDescriptor = (JSONArrayDescriptor) value;

        List<String> values = scalarValueDescriptor.getValue()
                                                         .stream().map(v -> (String)v.getValue())
                                                         .collect(toList());
        assertThat(values, contains("B", "C"));
    }

    @Test
    public void scanReturnsObjectWithAnArray() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/object-with-array.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/object-with-array.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONValueDescriptor<?> value = keyDescriptor.getValue();

        assertThat(value, instanceOf(JSONArrayDescriptor.class));

        JSONArrayDescriptor scalarValueDescriptor = (JSONArrayDescriptor) value;

        assertThat(scalarValueDescriptor.getValue(), hasSize(1));

        List<String> values = scalarValueDescriptor.getValue()
                                                   .stream().map(v -> (String) v.getValue())
                                                   .collect(toList());
        assertThat(values, contains("B"));

    }

    @Test
    public void scanReturnsObjectWithOneNumber() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/object-with-number.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("object-with-number.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        JSONObjectDescriptor container = (JSONObjectDescriptor) document.getContainer();

        assertThat(container.getKeys(), hasSize(1));

        JSONKeyDescriptor jsonKeyDescriptor = container.getKeys().get(0);

        Double value = (Double)jsonKeyDescriptor.getValue().getValue();

        assertThat(jsonKeyDescriptor.getName(), equalTo("A"));
        assertThatNumberIsEqual(value, 1);
    }

    @Test
    public void scanReturnsObjectWithMultipleNumbers() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/object-with-numbers.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        JSONObjectDescriptor container = (JSONObjectDescriptor) document.getContainer();

        assertThat(container.getKeys(), hasSize(5));

        JSONKeyDescriptor a = findKeyInDocument(container.getKeys(), "A");
        JSONKeyDescriptor b = findKeyInDocument(container.getKeys(), "B");
        JSONKeyDescriptor c = findKeyInDocument(container.getKeys(), "C");
        JSONKeyDescriptor d = findKeyInDocument(container.getKeys(), "D");
        JSONKeyDescriptor e = findKeyInDocument(container.getKeys(), "E");

        Double aValue = (Double)a.getValue().getValue();
        Double bValue = (Double)b.getValue().getValue();
        Double cValue = (Double)c.getValue().getValue();
        Double dValue = (Double)d.getValue().getValue();
        Double eValue = (Double)e.getValue().getValue();

        assertThatNumberIsEqual(aValue, 1);
        assertThatNumberIsEqual(bValue, -1);
        assertThatNumberIsEqual(cValue, 10);
        assertThatNumberIsEqual(dValue, -10);
        assertThatNumberIsEqual(eValue, -0.1);
    }

    @Test
    public void scanReturnsObjectWithObjects() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/object-with-objects.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);


        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("probes/valid/object-with-objects.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONValueDescriptor<?> value = keyDescriptor.getValue();

        assertThat(value, instanceOf(JSONObjectDescriptor.class));

        JSONObjectDescriptor scalarValueDescriptor = (JSONObjectDescriptor) value;

        JSONKeyDescriptor keyB = findKeyInDocument(scalarValueDescriptor.getKeys(), "B");
        JSONKeyDescriptor keyD = findKeyInDocument(scalarValueDescriptor.getKeys(), "D");
        assertThat(keyB.getName(), equalTo("B"));
        assertThat(keyD.getName(), equalTo("D"));

        JSONValueDescriptor<?> valueOfKeyD = keyD.getValue();
        assertThat(valueOfKeyD, instanceOf(JSONObjectDescriptor.class));

        JSONObjectDescriptor objectOfD = (JSONObjectDescriptor) valueOfKeyD;
        assertThat(objectOfD.getKeys(), empty());
    }

    @Test
    public void scanReturnsValueWithQuotationMark() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/string-value-with-quote-mark.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());


        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), equalTo("A"));

        JSONValueDescriptor<?> valueDescriptor = keyDescriptor.getValue();

        assertThat(valueDescriptor, instanceOf(JSONValueDescriptor.class));

        JSONValueDescriptor scalarValueDescriptor = (JSONValueDescriptor) valueDescriptor;

        Object object = scalarValueDescriptor.getValue();

        assertThat(object, Matchers.notNullValue());
        assertThat(object, Matchers.instanceOf(String.class));
        assertThat(object, Matchers.<Object>equalTo("B\"C"));
    }

    @Test
    public void scanReturnsValueWithEscapeSequences() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/string-value-with-possible-escape-sequences.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());


        JSONDocumentDescriptor document = file.getDocument();

        assertThat(document.getContainer(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) document.getContainer();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), equalTo("A"));

        JSONValueDescriptor<?> valueDescriptor = keyDescriptor.getValue();

        assertThat(valueDescriptor, instanceOf(JSONValueDescriptor.class));

        JSONValueDescriptor scalarValueDescriptor = (JSONValueDescriptor) valueDescriptor;

        Object object = scalarValueDescriptor.getValue();

        String expectedResult = ">\b\f\n\r\t<";

        assertThat(object, Matchers.notNullValue());
        assertThat(object, Matchers.instanceOf(String.class));
        assertThat(((String)object).length(), Matchers.is(expectedResult.length()));
        assertThat(object, Matchers.<Object>equalTo(expectedResult));
    }

    @Test
    public void scanReturnsCorrectedUnicodeString() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/string-value-with-unicode-signs.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());

        assertThat(file.getFileName(), Matchers.notNullValue());
        assertThat(file.getFileName(), endsWith("string-value-with-unicode-signs.json"));

        assertThat(file.getDocument(), Matchers.notNullValue());

        JSONDocumentDescriptor document = file.getDocument();

        JSONObjectDescriptor container = (JSONObjectDescriptor) document.getContainer();

        assertThat(container.getKeys(), hasSize(1));

        JSONKeyDescriptor jsonKeyDescriptor = container.getKeys().get(0);

        assertThat(jsonKeyDescriptor.getName(), equalTo("A"));

        JSONValueDescriptor<?> value = jsonKeyDescriptor.getValue();

        assertThat(value, instanceOf(JSONScalarValueDescriptor.class));

        JSONScalarValueDescriptor scalarValueDescriptor = (JSONScalarValueDescriptor) value;

        Object rawValue = scalarValueDescriptor.getValue();

        // дом культуры
        assertThat(rawValue, Matchers.<Object>equalTo("дом культуры"));
    }

    private JSONKeyDescriptor findKeyInDocument(List<JSONKeyDescriptor> keys, String name) {
        JSONKeyDescriptor result = null;

        for (JSONKeyDescriptor key : keys) {
            if (key.getName().equals(name)) {
                result = key;
                break;
            }
        }

        return result;
    }

    public void assertThatNumberIsEqual(Number actual, Number expected) {
        BigDecimal left = new BigDecimal(actual.doubleValue());
        BigDecimal right = new BigDecimal(expected.doubleValue());

        if (left.compareTo(right) != 0) {
            String msg = "Expected <" + right + ">, but is <" + left + ">";
            throw new AssertionError(msg);
        }
    }
}
