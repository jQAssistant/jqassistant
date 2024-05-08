package com.buschmais.jqassistant.plugin.json.impl.scanner;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.json.api.model.JSONArrayDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONFileDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONKeyDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONObjectDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONScalarValueDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONValueDescriptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class JSONFileScannerPluginIT extends AbstractPluginIT {

    @BeforeEach
    void startTransaction() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTransaction() {
        if (store.hasActiveTransaction()) {
            store.commitTransaction();
        }
    }

    @Test
    void scanReturnsFileDescriptorWithCorrectFileName() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/true-false-null.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/true-false-null.json");
    }

    @Test
    void scanReturnsObjectWithOneKeyValuePair() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/object-one-key-value-pair.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/object-one-key-value-pair.json");

        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");
        JSONValueDescriptor value = keyDescriptor.getValue();
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOfSatisfying(JSONScalarValueDescriptor.class, scalarValue -> assertThat(scalarValue.getValue()).isEqualTo("B"));

    }

    @Test
    void scanReturnsObjectWithTwoKeyValuePairs() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/object-two-key-value-pairs.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/object-two-key-value-pairs.json");
        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(2);

        JSONKeyDescriptor keyDescriptorA = findKeyInDocument(jsonObject.getKeys(), "A");
        JSONKeyDescriptor keyDescriptorB = findKeyInDocument(jsonObject.getKeys(), "C");

        assertThat(keyDescriptorA.getName()).isEqualTo("A");
        assertThat(keyDescriptorA.getValue()).isNotNull();
        assertThat(keyDescriptorA.getValue()).isInstanceOfSatisfying(JSONScalarValueDescriptor.class, scalar -> assertThat(scalar.getValue()).isEqualTo("B"));
        assertThat(keyDescriptorB.getName()).isEqualTo("C");
        assertThat(keyDescriptorB.getValue()).isInstanceOfSatisfying(JSONScalarValueDescriptor.class, scalar -> assertThat(scalar.getValue()).isEqualTo("D"));
    }

    @Test
    void scanReturnsObjectWithTrueFalseAndNullValue() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/true-false-null.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/true-false-null.json");
        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(3);

        JSONKeyDescriptor keyDescriptorA = findKeyInDocument(jsonObject.getKeys(), "A");
        JSONKeyDescriptor keyDescriptorB = findKeyInDocument(jsonObject.getKeys(), "B");
        JSONKeyDescriptor keyDescriptorC = findKeyInDocument(jsonObject.getKeys(), "C");

        assertThat(keyDescriptorA.getName()).isEqualTo("A");
        assertThat(keyDescriptorA.getValue()).isNotNull()
            .isInstanceOfSatisfying(JSONScalarValueDescriptor.class, scalar -> assertThat(scalar.getValue()).isEqualTo(Boolean.TRUE));

        assertThat(keyDescriptorB.getName()).isEqualTo("B");
        assertThat(keyDescriptorB.getValue()).isNotNull();
        assertThat(keyDescriptorB.getValue()).isInstanceOfSatisfying(JSONScalarValueDescriptor.class,
            scalar -> assertThat(scalar.getValue()).isEqualTo(Boolean.FALSE));

        assertThat(keyDescriptorC.getName()).isEqualTo("C");
        assertThat(keyDescriptorC.getValue()).isNotNull();
        assertThat(keyDescriptorC.getValue()).isInstanceOfSatisfying(JSONScalarValueDescriptor.class, scalar -> assertThat(scalar.getValue()).isNull());
    }

    @Test
    void scanReturnsEmptyArray() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/array-empty.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/array-empty.json");

        assertThat(file.getValue()).isNotNull();

        JSONArrayDescriptor jsonArray = (JSONArrayDescriptor) file.getValue();

        assertThat(jsonArray.getValues()).isEmpty();
    }

    @Test
    void scanReturnsArrayWithOneValue() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/array-one-value.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/array-one-value.json");

        assertThat(file.getValue()).isNotNull();

        JSONArrayDescriptor jsonArray = (JSONArrayDescriptor) file.getValue();

        assertThat(jsonArray.getValues()).hasSize(1);

        JSONDescriptor valueDescriptor = jsonArray.getValues()
            .get(0);

        assertThat(valueDescriptor).isInstanceOf(JSONScalarValueDescriptor.class);
        JSONScalarValueDescriptor jsonValueDescriptor = (JSONScalarValueDescriptor) valueDescriptor;
        assertThat(jsonValueDescriptor.getValue()).isEqualTo("ABC");
    }

    @Test
    void scanReturnsObjectWithEmptyArray() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/object-with-array-empty.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/object-with-array-empty.json");
        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");

        JSONArrayDescriptor arrayValueDescriptor = (JSONArrayDescriptor) keyDescriptor.getValue();

        assertThat(arrayValueDescriptor.getValues()).isEmpty();
    }

    @Test
    void scanReturnsObjectWithObject() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/object-with-object-empty.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/object-with-object-empty.json");

        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");

        JSONObjectDescriptor objectValue = (JSONObjectDescriptor) keyDescriptor.getValue();

        assertThat(objectValue).isNotNull();
        assertThat(objectValue.getKeys()).isEmpty();
    }

    @Test
    void scannerCanHandleBlockCommentInObject() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/block-comment-in-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        JSONObjectDescriptor object = (JSONObjectDescriptor) file.getValue();

        assertThat(object.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = object.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");

        JSONScalarValueDescriptor value = (JSONScalarValueDescriptor) keyDescriptor.getValue();

        Object objectTwo = value.getValue();

        assertThat(objectTwo).isNotNull();
        assertThat(objectTwo).isInstanceOf(String.class);
        assertThat(objectTwo).isEqualTo("B");
    }

    @Test()
    void scannerCanHandleEmptyFile() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/invalid/empty-file.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file.isValid()).isEqualTo(false);
    }

    @Test
    void parserCopesWithInvalidJSONFile() throws Exception {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/invalid/json-file-as-template.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file.isValid()).isEqualTo(false);
    }

    @Test
    void parserHandlesLineCommentAfterObjectCorrectly() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/line-comment-after-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/line-comment-after-object.json");

        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");

        JSONScalarValueDescriptor value = (JSONScalarValueDescriptor) keyDescriptor.getValue();

        Object object = value.getValue();

        assertThat(object).isNotNull();
        assertThat(object).isInstanceOf(String.class);
        assertThat(object).isEqualTo("B");
    }

    @Test
    void parserHandlesLineCommentBeforeObjectCorrectly() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/line-comment-before-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/line-comment-before-object.json");

        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");

        JSONScalarValueDescriptor value = (JSONScalarValueDescriptor) keyDescriptor.getValue();

        Object object = value.getValue();

        assertThat(object).isNotNull();
        assertThat(object).isInstanceOf(String.class);
        assertThat(object).isEqualTo("B");
    }

    @Test
    void parserHandlesLineCommentInObjectCorrectly() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/line-comment-in-object.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/line-comment-in-object.json");

        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");

        JSONScalarValueDescriptor value = (JSONScalarValueDescriptor) keyDescriptor.getValue();

        Object object = value.getValue();

        assertThat(object).isNotNull();
        assertThat(object).isInstanceOf(String.class);
        assertThat(object).isEqualTo("B");
    }

    @Test
    void scanReturnsObjectWithArrayOfTwoElements() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/object-with-array-two-elements.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/object-with-array-two-elements.json");

        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");

        JSONArrayDescriptor array = (JSONArrayDescriptor) keyDescriptor.getValue();

        List<String> values = array.getValues()
            .stream()
            .map(v -> (JSONScalarValueDescriptor) v)
            .map(v -> (String) v.getValue())
            .collect(toList());

        assertThat(values).hasSize(2);
        assertThat(values).contains("B", "C");
    }

    @Test
    void scanReturnsObjectWithAnArray() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/object-with-array.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/object-with-array.json");

        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");

        JSONArrayDescriptor array = (JSONArrayDescriptor) keyDescriptor.getValue();

        assertThat(array.getValues()).hasSize(1);

        List<String> values = array.getValues()
            .stream()
            .map(v -> (JSONScalarValueDescriptor) v)
            .map(v -> (String) v.getValue())
            .collect(toList());
        assertThat(values).contains("B");
    }

    @Test
    void scanReturnsObjectWithOneNumber() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/object-with-number.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("object-with-number.json");
        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor object = (JSONObjectDescriptor) file.getValue();

        assertThat(object.getKeys()).hasSize(1);

        JSONKeyDescriptor jsonKeyDescriptor = object.getKeys()
            .get(0);

        Number value = (Number) ((JSONScalarValueDescriptor) jsonKeyDescriptor.getValue()).getValue();

        assertThat(jsonKeyDescriptor.getName()).isEqualTo("A");
        assertThatNumberIsEqual(value, 1);
    }

    @Test
    void scanReturnsObjectWithMultipleNumbers() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/object-with-numbers.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();

        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor container = (JSONObjectDescriptor) file.getValue();

        assertThat(container.getKeys()).hasSize(5);

        JSONKeyDescriptor a = findKeyInDocument(container.getKeys(), "A");
        JSONKeyDescriptor b = findKeyInDocument(container.getKeys(), "B");
        JSONKeyDescriptor c = findKeyInDocument(container.getKeys(), "C");
        JSONKeyDescriptor d = findKeyInDocument(container.getKeys(), "D");
        JSONKeyDescriptor e = findKeyInDocument(container.getKeys(), "E");

        Number aValue = (Number) ((JSONScalarValueDescriptor) a.getValue()).getValue();
        Number bValue = (Number) ((JSONScalarValueDescriptor) b.getValue()).getValue();
        Number cValue = (Number) ((JSONScalarValueDescriptor) c.getValue()).getValue();
        Number dValue = (Number) ((JSONScalarValueDescriptor) d.getValue()).getValue();
        Number eValue = (Number) ((JSONScalarValueDescriptor) e.getValue()).getValue();

        assertThatNumberIsEqual(aValue, 1);
        assertThatNumberIsEqual(bValue, -1);
        assertThatNumberIsEqual(cValue, 10);
        assertThatNumberIsEqual(dValue, -10);
        assertThatNumberIsEqual(eValue, -0.1);
    }

    @Test
    void scanReturnsObjectWithObjects() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/object-with-objects.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("probes/valid/object-with-objects.json");

        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");

        JSONObjectDescriptor object = (JSONObjectDescriptor) keyDescriptor.getValue();

        JSONKeyDescriptor keyB = findKeyInDocument(object.getKeys(), "B");
        JSONKeyDescriptor keyD = findKeyInDocument(object.getKeys(), "D");
        assertThat(keyB.getName()).isEqualTo("B");
        assertThat(keyD.getName()).isEqualTo("D");

        JSONObjectDescriptor objectOfKeyD = (JSONObjectDescriptor) keyD.getValue();
        assertThat(objectOfKeyD.getKeys()).isEmpty();
    }

    @Test
    void scanReturnsValueWithQuotationMark() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/string-value-with-quote-mark.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        JSONObjectDescriptor object = (JSONObjectDescriptor) file.getValue();

        assertThat(object.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = object.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");

        JSONScalarValueDescriptor value = (JSONScalarValueDescriptor) keyDescriptor.getValue();

        Object string = value.getValue();

        assertThat(string).isNotNull();
        assertThat(string).isInstanceOf(String.class);
        assertThat(string).isEqualTo("B\"C");
    }

    @Test
    void scanReturnsValueWithEscapeSequences() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/string-value-with-possible-escape-sequences.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        JSONObjectDescriptor jsonObject = (JSONObjectDescriptor) file.getValue();

        assertThat(jsonObject.getKeys()).hasSize(1);

        JSONKeyDescriptor keyDescriptor = jsonObject.getKeys()
            .get(0);

        assertThat(keyDescriptor.getName()).isEqualTo("A");

        JSONScalarValueDescriptor scalarValue = (JSONScalarValueDescriptor) keyDescriptor.getValue();

        Object object = scalarValue.getValue();

        String expectedResult = ">\b\f\n\r\t<";

        assertThat(object).isNotNull();
        assertThat(object).isInstanceOf(String.class);
        assertThat(((String) object).length()).isEqualTo(expectedResult.length());
        assertThat(object).isEqualTo(expectedResult);
    }

    @Test
    void scanReturnsCorrectedUnicodeString() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginIT.class), "/probes/valid/string-value-with-unicode-signs.json");

        Scanner scanner = getScanner();
        JSONFileDescriptor file = scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        assertThat(file).as("Scanner must be able to scan the resource and to return a descriptor.")
            .isNotNull();

        assertThat(file.getFileName()).isNotNull();
        assertThat(file.getFileName()).endsWith("string-value-with-unicode-signs.json");

        assertThat(file.getValue()).isNotNull();

        JSONObjectDescriptor container = (JSONObjectDescriptor) file.getValue();

        assertThat(container.getKeys()).hasSize(1);

        JSONKeyDescriptor jsonKeyDescriptor = container.getKeys()
            .get(0);

        assertThat(jsonKeyDescriptor.getName()).isEqualTo("A");

        JSONScalarValueDescriptor value = (JSONScalarValueDescriptor) jsonKeyDescriptor.getValue();

        Object rawValue = value.getValue();

        // дом культуры
        assertThat(rawValue).isEqualTo("дом культуры");
    }

    private JSONKeyDescriptor findKeyInDocument(List<JSONKeyDescriptor> keys, String name) {
        JSONKeyDescriptor result = null;

        for (JSONKeyDescriptor key : keys) {
            if (key.getName()
                .equals(name)) {
                result = key;
                break;
            }
        }

        return result;
    }

    void assertThatNumberIsEqual(Number actual, Number expected) {
        BigDecimal left = new BigDecimal(actual.doubleValue());
        BigDecimal right = new BigDecimal(expected.doubleValue());

        if (left.compareTo(right) != 0) {
            String msg = "Expected <" + right + ">, but is <" + left + ">";
            throw new AssertionError(msg);
        }
    }
}
