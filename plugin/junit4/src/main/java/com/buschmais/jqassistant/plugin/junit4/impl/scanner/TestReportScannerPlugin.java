package com.buschmais.jqassistant.plugin.junit4.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.junit4.impl.store.descriptor.TestCaseDescriptor;
import com.buschmais.jqassistant.plugin.junit4.impl.store.descriptor.TestSuiteDescriptor;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Locale;

public class TestReportScannerPlugin implements FileScannerPlugin<TestSuiteDescriptor> {

    private final NumberFormat timeFormat = NumberFormat.getInstance(Locale.US);

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return !isDirectory && file.matches(".*TEST-.*\\.xml");
    }

    @Override
    public TestSuiteDescriptor scanFile(Store store, StreamSource streamSource) throws IOException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader;
        try {
            reader = inputFactory.createXMLEventReader(streamSource.getInputStream());
        } catch (XMLStreamException e) {
            throw new IOException("Cannot create XML event reader.", e);
        }
        TestSuiteDescriptor testSuiteDescriptor = null;
        TestCaseDescriptor testCaseDescriptor = null;
        while (reader.hasNext()) {
            XMLEvent event = (XMLEvent) reader.next();
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String elementName = element.getName().getLocalPart();
                Iterator attributes = element.getAttributes();
                switch (elementName) {
                    case "testsuite":
                        testSuiteDescriptor = store.create(TestSuiteDescriptor.class);
                        testSuiteDescriptor.setFileName(streamSource.getSystemId());
                        while (attributes.hasNext()) {
                            Attribute attribute = (Attribute) attributes.next();
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
                            Attribute attribute = (Attribute) attributes.next();
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
    }

    private float parseTime(String value) throws IOException {
        try {
            return timeFormat.parse(value).floatValue();
        } catch (ParseException e) {
            throw new IOException("Cannot parse time.", e);
        }
    }

    @Override
    public TestSuiteDescriptor scanDirectory(Store store, String name) throws IOException {
        return null;
    }
}
