package com.buschmais.jqassistant.plugin.json.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugins.json.api.model.JSONArrayDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONArrayValueDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONDocumentDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONFileDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONKeyDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONObjectDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONObjectValueDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONScalarValueDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONValueDescriptor;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
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

        assertThat(jsonArray.getValues(), Matchers.empty());
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

        assertThat(jsonArray.getValues(), hasSize(1));

        JSONValueDescriptor<?> valueDescriptor = jsonArray.getValues().get(0);

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
        assertThat(keyDescriptor.getValue(), Matchers.instanceOf(JSONArrayValueDescriptor.class));

        JSONArrayValueDescriptor arrayValueDescriptor = (JSONArrayValueDescriptor) keyDescriptor.getValue();

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

        assertThat(value, instanceOf(JSONObjectValueDescriptor.class));

        JSONObjectValueDescriptor objectValueDescriptor = (JSONObjectValueDescriptor) value;

        JSONObjectDescriptor object = objectValueDescriptor.getValue();

        assertThat(object, Matchers.notNullValue());
        assertThat(object, Matchers.instanceOf(JSONObjectDescriptor.class));
    }

    @Test
    public void scanReturnsXXXXX() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/block-comment-in-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        throw new RuntimeException("Test not implemented!");
    }

    @Test
    public void scanReturnsXXXXXX() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/empty-file.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        throw new RuntimeException("Test not implemented!");
    }

    @Test
    public void scanReturnsCorrectObjectIfLineCommentIfAfterTheObject() {
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
    public void scanReturnsXXXXXXXX() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/line-comment-before-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        throw new RuntimeException("Test not implemented!");
    }

    @Test
    public void scanReturnsXXXXXXXXX() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/line-comment-in-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        throw new RuntimeException("Test not implemented!");
    }

    @Test
    public void scanReturnsXXXXXXXXXX() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/object-with-array-two-elements.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        throw new RuntimeException("Test not implemented!");
    }

    @Test
    public void scanReturnsXXXXXXXXXXXX() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/object-with-array.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        throw new RuntimeException("Test not implemented!");
    }

    @Test
    public void scanReturnsXXXXXXXXXXXXXXX() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/object-with-number.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        throw new RuntimeException("Test not implemented!");
    }

    @Test
    public void scanReturnsXXXXXXXXXXXXXXXXX() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/object-with-objects.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        throw new RuntimeException("Test not implemented!");
    }

    @Test
    public void scanReturnsXXXX__X() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/string-value-with-quote-mark.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        throw new RuntimeException("Test not implemented!");
    }

    @Test
    public void scanReturnsXXXX___X() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/string-value-with-unicode-signs.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        throw new RuntimeException("Test not implemented!");
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
}
