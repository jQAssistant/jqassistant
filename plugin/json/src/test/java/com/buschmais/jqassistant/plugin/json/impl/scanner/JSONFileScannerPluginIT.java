package com.buschmais.jqassistant.plugin.json.impl.scanner;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.json.api.model.JSONArrayDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONFileDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONKeyDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONObjectDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONScalarValueDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONValueDescriptor;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
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

        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), CoreMatchers.equalTo("A"));
        assertThat(keyDescriptor.getScalarValue().getValue(), notNullValue());
        assertThat(keyDescriptor.getScalarValue().getValue(), IsEqual.<Object>equalTo("B"));
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
        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(2));

        JSONKeyDescriptor keyDescriptorA = findKeyInDocument(jsonObject.getKeys(), "A");
        JSONKeyDescriptor keyDescriptorB = findKeyInDocument(jsonObject.getKeys(), "C");

        assertThat(keyDescriptorA.getName(), CoreMatchers.equalTo("A"));
        assertThat(keyDescriptorA.getScalarValue(), Matchers.notNullValue());
        assertThat(keyDescriptorA.getScalarValue().getValue(), Matchers.<Object>equalTo("B"));
        assertThat(keyDescriptorB.getName(), CoreMatchers.equalTo("C"));
        assertThat(keyDescriptorB.getScalarValue().getValue(), Matchers.<Object>equalTo("D"));
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
        assertThat(file.getObject(), Matchers.notNullValue());


        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(3));

        JSONKeyDescriptor keyDescriptorA = findKeyInDocument(jsonObject.getKeys(), "A");
        JSONKeyDescriptor keyDescriptorB = findKeyInDocument(jsonObject.getKeys(), "B");
        JSONKeyDescriptor keyDescriptorC = findKeyInDocument(jsonObject.getKeys(), "C");

        assertThat(keyDescriptorA.getName(), CoreMatchers.equalTo("A"));
        assertThat(keyDescriptorA.getScalarValue(), Matchers.notNullValue());
        assertThat(keyDescriptorA.getScalarValue().getValue(), Matchers.<Object>equalTo(Boolean.TRUE));

        assertThat(keyDescriptorB.getName(), CoreMatchers.equalTo("B"));
        assertThat(keyDescriptorB.getScalarValue(), Matchers.notNullValue());
        assertThat(keyDescriptorB.getScalarValue().getValue(), Matchers.<Object>equalTo(Boolean.FALSE));

        assertThat(keyDescriptorC.getName(), CoreMatchers.equalTo("C"));
        assertThat(keyDescriptorC.getScalarValue(), Matchers.nullValue());
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

        assertThat(file.getArray(), Matchers.notNullValue());

        JSONArrayDescriptor jsonArray = file.getArray();

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

        assertThat(file.getArray(), Matchers.notNullValue());

        JSONArrayDescriptor jsonArray = file.getArray();

        assertThat(jsonArray.getValues(), hasSize(1));

        JSONDescriptor valueDescriptor = jsonArray.getValues().get(0);

        assertThat(valueDescriptor, Matchers.instanceOf(JSONValueDescriptor.class));
        JSONValueDescriptor jsonValueDescriptor = (JSONValueDescriptor) valueDescriptor;
        assertThat(jsonValueDescriptor.getValue(), Matchers.<Object>equalTo("ABC"));
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
        assertThat(file.getObject(), Matchers.notNullValue());


        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.<Object>equalTo("A"));

        JSONArrayDescriptor arrayValueDescriptor = keyDescriptor.getArray();

        assertThat(arrayValueDescriptor.getValues(), empty());
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

        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONObjectDescriptor objectValue = keyDescriptor.getObject();

        assertThat(objectValue, Matchers.notNullValue());
        assertThat(objectValue.getKeys(), empty());
    }

    @Test
    public void scannerCanHandleBlockCommentInObject() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/block-comment-in-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        JSONObjectDescriptor object = file.getObject();

        assertThat(object.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = object.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONScalarValueDescriptor value = keyDescriptor.getScalarValue();

        Object objectTwo = value.getValue();

        assertThat(objectTwo, Matchers.notNullValue());
        assertThat(objectTwo, Matchers.instanceOf(String.class));
        assertThat(objectTwo, Matchers.<Object>equalTo("B"));
    }

    @Test()
    public void scannerCanHandleEmptyFile() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/invalid/empty-file.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file.isValid(), Matchers.equalTo(false));
    }

    @Test
    public void parserCopesWithInvalidJSONFile() throws Exception {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/invalid/json-file-as-template.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file.isValid(), Matchers.equalTo(false));
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

        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONScalarValueDescriptor value = keyDescriptor.getScalarValue();

        Object object = value.getValue();

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

        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONScalarValueDescriptor value = keyDescriptor.getScalarValue();

        Object object = value.getValue();

        assertThat(object, Matchers.notNullValue());
        assertThat(object, Matchers.instanceOf(String.class));
        assertThat(object, Matchers.equalTo("B"));
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

        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONScalarValueDescriptor value = keyDescriptor.getScalarValue();

        Object object = value.getValue();

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

        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONArrayDescriptor array = keyDescriptor.getArray();

        List<String> values = array.getValues().stream()
                                   .map(v -> (JSONValueDescriptor)v)
                                   .map(v -> (String) v.getValue())
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

        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONArrayDescriptor array = keyDescriptor.getArray();

        assertThat(array.getValues(), hasSize(1));

        List<String> values = array.getValues()
                                   .stream()
                                   .map(v -> (JSONValueDescriptor)v)
                                   .map(v -> (String) v.getValue())
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
        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor object = file.getObject();

        assertThat(object.getKeys(), hasSize(1));

        JSONKeyDescriptor jsonKeyDescriptor = object.getKeys().get(0);

        Double value = (Double)jsonKeyDescriptor.getScalarValue().getValue();

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

        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor container = file.getObject();

        assertThat(container.getKeys(), hasSize(5));

        JSONKeyDescriptor a = findKeyInDocument(container.getKeys(), "A");
        JSONKeyDescriptor b = findKeyInDocument(container.getKeys(), "B");
        JSONKeyDescriptor c = findKeyInDocument(container.getKeys(), "C");
        JSONKeyDescriptor d = findKeyInDocument(container.getKeys(), "D");
        JSONKeyDescriptor e = findKeyInDocument(container.getKeys(), "E");

        Double aValue = (Double)a.getScalarValue().getValue();
        Double bValue = (Double)b.getScalarValue().getValue();
        Double cValue = (Double)c.getScalarValue().getValue();
        Double dValue = (Double)d.getScalarValue().getValue();
        Double eValue = (Double)e.getScalarValue().getValue();

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

        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), Matchers.equalTo("A"));

        JSONObjectDescriptor object = keyDescriptor.getObject();

        JSONKeyDescriptor keyB = findKeyInDocument(object.getKeys(), "B");
        JSONKeyDescriptor keyD = findKeyInDocument(object.getKeys(), "D");
        assertThat(keyB.getName(), equalTo("B"));
        assertThat(keyD.getName(), equalTo("D"));

        JSONObjectDescriptor objectOfKeyD = keyD.getObject();
        assertThat(objectOfKeyD.getKeys(), empty());
    }

    @Test
    public void scanReturnsValueWithQuotationMark() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/string-value-with-quote-mark.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());


        JSONObjectDescriptor object = file.getObject();

        assertThat(object.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = object.getKeys().get(0);

        assertThat(keyDescriptor.getName(), equalTo("A"));

        JSONScalarValueDescriptor value = keyDescriptor.getScalarValue();

        Object string = value.getValue();

        assertThat(string, Matchers.notNullValue());
        assertThat(string, Matchers.instanceOf(String.class));
        assertThat(string, Matchers.<Object>equalTo("B\"C"));
    }

    @Test
    public void scanReturnsValueWithEscapeSequences() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class),
                                 "/probes/valid/string-value-with-possible-escape-sequences.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat("Scanner must be able to scan the resource and to return a descriptor.",
                   file, notNullValue());


        JSONObjectDescriptor jsonObject = file.getObject();

        assertThat(jsonObject.getKeys(), hasSize(1));

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys().get(0);

        assertThat(keyDescriptor.getName(), equalTo("A"));

        JSONScalarValueDescriptor scalarValue = keyDescriptor.getScalarValue();

        Object object = scalarValue.getValue();

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

        assertThat(file.getObject(), Matchers.notNullValue());

        JSONObjectDescriptor container = file.getObject();

        assertThat(container.getKeys(), hasSize(1));

        JSONKeyDescriptor jsonKeyDescriptor = container.getKeys().get(0);

        assertThat(jsonKeyDescriptor.getName(), equalTo("A"));

        JSONScalarValueDescriptor value = jsonKeyDescriptor.getScalarValue();

        Object rawValue = value.getValue();

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
