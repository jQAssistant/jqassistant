package com.buschmais.jqassistant.plugin.junit.impl.scanner;

import static com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope.TESTREPORTS;

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
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.junit.api.model.TestCaseDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.model.TestSuiteDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XmlScope;

@Requires(FileDescriptor.class)
public class TestReportScannerPlugin extends AbstractScannerPlugin<FileResource, TestSuiteDescriptor> {

    private final NumberFormat timeFormat = NumberFormat.getInstance(Locale.US);

    private XMLInputFactory inputFactory;

    @Override
    public void initialize() {
        inputFactory = XMLInputFactory.newInstance();
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return TESTREPORTS.equals(scope) && path.matches(".*TEST-.*\\.xml");
    }

    @Override
    public TestSuiteDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        XmlFileDescriptor xmlFileDescriptor = scanner.scan(item, path, XmlScope.DOCUMENT);
        try (InputStream stream = item.createStream()) {
            XMLEventReader reader = inputFactory.createXMLEventReader(stream);
            TestSuiteDescriptor testSuiteDescriptor = null;
            TestCaseDescriptor testCaseDescriptor = null;
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    StartElement element = event.asStartElement();
                    String elementName = element.getName().getLocalPart();
                    @SuppressWarnings("unchecked")
                    Iterator<Attribute> attributes = element.getAttributes();
                    switch (elementName) {
                    case "testsuite":
                        testSuiteDescriptor = scanner.getContext().getStore().addDescriptorType(xmlFileDescriptor, TestSuiteDescriptor.class);
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
                        testCaseDescriptor = scanner.getContext().getStore().create(TestCaseDescriptor.class);
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
                        break;
                    case "error":
                        testCaseDescriptor.setResult(TestCaseDescriptor.Result.ERROR);
                        break;
                    case "skipped":
                        testCaseDescriptor.setResult(TestCaseDescriptor.Result.SKIPPED);
                        break;
                    }
                }
            }
            return testSuiteDescriptor;
        } catch (XMLStreamException e) {
            throw new IOException("Cannot read XML document.", e);
        }
    }

    private float parseTime(String value) throws IOException {
        try {
            return timeFormat.parse(value).floatValue();
        } catch (ParseException e) {
            throw new IOException("Cannot parse time.", e);
        }
    }
}
