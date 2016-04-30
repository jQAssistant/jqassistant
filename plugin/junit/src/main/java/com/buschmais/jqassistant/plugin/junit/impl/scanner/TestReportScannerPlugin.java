package com.buschmais.jqassistant.plugin.junit.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.junit.api.model.*;
import com.buschmais.jqassistant.plugin.xml.api.scanner.AbstractXmlFileScannerPlugin;

import static com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope.TESTREPORTS;

@Requires(FileDescriptor.class)
public class TestReportScannerPlugin extends AbstractXmlFileScannerPlugin<TestSuiteDescriptor> {

    private final NumberFormat timeFormat = NumberFormat.getInstance(Locale.US);

    private XMLInputFactory inputFactory;

    @Override
    public void initialize() {
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return TESTREPORTS.equals(scope) && path.matches(".*TEST-.*\\.xml");
    }

    @Override
    public TestSuiteDescriptor scan(FileResource item, TestSuiteDescriptor testSuiteDescriptor, String path, Scope scope, Scanner scanner)
            throws IOException {
        Store store = scanner.getContext().getStore();
        try (InputStream stream = item.createStream()) {
            XMLEventReader reader = inputFactory.createXMLEventReader(stream);
            StringBuilder characters = null;
            TestCaseDescriptor testCaseDescriptor = null;
            TestCaseDetailDescriptor testCaseDetailDescriptor = null;
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    StartElement element = event.asStartElement();
                    String elementName = element.getName().getLocalPart();
                    @SuppressWarnings("unchecked")
                    Iterator<Attribute> attributes = element.getAttributes();
                    switch (elementName) {
                        case "testsuite":
                            while (attributes.hasNext()) {
                                Attribute attribute = attributes.next();
                                String attributeName = attribute.getName().getLocalPart();
                                String value = attribute.getValue();
                                switch (attributeName) {
                                    case "name":
                                        testSuiteDescriptor.setName(value);
                                        break;
                                    case "time":
                                        testSuiteDescriptor.setTime(parseTime(value));
                                        break;
                                    case "tests":
                                        testSuiteDescriptor.setTests(Integer.parseInt(value));
                                        break;
                                    case "failures":
                                        testSuiteDescriptor.setFailures(Integer.parseInt(value));
                                        break;
                                    case "errors":
                                        testSuiteDescriptor.setErrors(Integer.parseInt(value));
                                        break;
                                    case "skipped":
                                        testSuiteDescriptor.setSkipped(Integer.parseInt(value));
                                        break;
                                }
                            }
                            break;
                        case "testcase":
                            testCaseDescriptor = store.create(TestCaseDescriptor.class);
                            testCaseDescriptor.setResult(TestCaseDescriptor.Result.SUCCESS);
                            testSuiteDescriptor.getTestCases().add(testCaseDescriptor);
                            while (attributes.hasNext()) {
                                Attribute attribute = attributes.next();
                                String attributeName = attribute.getName().getLocalPart();
                                String value = attribute.getValue();
                                switch (attributeName) {
                                    case "name":
                                        testCaseDescriptor.setName(value);
                                        break;
                                    case "time":
                                        testCaseDescriptor.setTime(parseTime(value));
                                        break;
                                    case "classname":
                                        testCaseDescriptor.setClassName(value);
                                        break;
                                }
                            }
                            break;
                        case "failure":
                            testCaseDescriptor.setResult(TestCaseDescriptor.Result.FAILURE);
                            testCaseDetailDescriptor = store.create(TestCaseFailureDescriptor.class);
                            setTestCaseDetailAttributes(testCaseDetailDescriptor, attributes);
                            break;
                        case "error":
                            testCaseDescriptor.setResult(TestCaseDescriptor.Result.ERROR);
                            testCaseDetailDescriptor = store.create(TestCaseErrorDescriptor.class);
                            setTestCaseDetailAttributes(testCaseDetailDescriptor, attributes);
                            break;
                        case "skipped":
                            testCaseDescriptor.setResult(TestCaseDescriptor.Result.SKIPPED);
                            break;
                    }
                } else if (event.isCharacters()) {
                    String data = event.asCharacters().getData();
                    if (characters == null) {
                        characters = new StringBuilder(data);
                    } else {
                        characters.append(data);
                    }
                } else if (event.isEndElement()) {
                    EndElement element = event.asEndElement();
                    String name = element.getName().getLocalPart();
                    switch (name) {
                        case "failure":
                            characters = setTestCaseDetailData(testCaseDetailDescriptor, characters);
                            testCaseDescriptor.setFailure((TestCaseFailureDescriptor) testCaseDetailDescriptor);
                            break;
                        case "error":
                            characters = setTestCaseDetailData(testCaseDetailDescriptor, characters);
                            testCaseDescriptor.setError((TestCaseErrorDescriptor) testCaseDetailDescriptor);
                            break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new IOException("Cannot read XML document.", e);
        }
        return testSuiteDescriptor;
    }

    private void setTestCaseDetailAttributes(TestCaseDetailDescriptor testCaseDetailDescriptor, Iterator<Attribute> attributes) {
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            String attributeName = attribute.getName().getLocalPart();
            String value = attribute.getValue();
            switch (attributeName) {
                case "type":
                    testCaseDetailDescriptor.setType(value);
                    break;
            }
        }
    }

    private StringBuilder setTestCaseDetailData(TestCaseDetailDescriptor testCaseDetailDescriptor, StringBuilder characters) {
        testCaseDetailDescriptor.setDetails(characters.toString());
        return new StringBuilder();
    }

    private float parseTime(String value) throws IOException {
        try {
            return timeFormat.parse(value).floatValue();
        } catch (ParseException e) {
            throw new IOException("Cannot parse time.", e);
        }
    }
}
