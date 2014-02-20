package com.buschmais.jqassistant.plugin.junit4.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ClassTypeDescriptor;
import com.buschmais.jqassistant.plugin.junit4.impl.store.TestCaseDescriptor;
import com.buschmais.jqassistant.plugin.junit4.impl.store.TestSuiteDescriptor;

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

public class TestReportScanner implements FileScannerPlugin<TestSuiteDescriptor> {

    private final NumberFormat timeFormat = NumberFormat.getInstance(Locale.US);

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return file.matches(".*TEST-.*\\.xml");
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
                if ("testsuite".equals(elementName)) {
                    testSuiteDescriptor = store.create(TestSuiteDescriptor.class);
                    Iterator attributes = element.getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = (Attribute) attributes.next();
                        String attributeName = attribute.getName().getLocalPart();
                        String value = attribute.getValue();
                        if ("name".equals(attributeName)) {
                            testSuiteDescriptor.setName(value);
                        } else if ("time".equals(attributeName)) {
                            testSuiteDescriptor.setTime(parseTime(value));
                        } else if ("tests".equals(attributeName)) {
                            testSuiteDescriptor.setTests(Integer.parseInt(value));
                        } else if ("failures".equals(attributeName)) {
                            testSuiteDescriptor.setFailures(Integer.parseInt(value));
                        } else if ("errors".equals(attributeName)) {
                            testSuiteDescriptor.setErrors(Integer.parseInt(value));
                        } else if ("skipped".equals(attributeName)) {
                            testSuiteDescriptor.setSkipped(Integer.parseInt(value));
                        }
                    }
                } else if ("testsuite".equals(elementName)) {
                    testCaseDescriptor = store.create(TestCaseDescriptor.class);
                    testCaseDescriptor.setResult(TestCaseDescriptor.Result.SUCCESS);
                    testSuiteDescriptor.getTestCases().add(testCaseDescriptor);
                    Iterator attributes = element.getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = (Attribute) attributes.next();
                        String attributeName = attribute.getName().getLocalPart();
                        String value = attribute.getValue();
                        if ("name".equals(attributeName)) {
                            testCaseDescriptor.setName(value);
                        } else if ("time".equals(attributeName)) {
                            testCaseDescriptor.setTime(parseTime(value));
                        } else if ("classname".equals(attributeName)) {
                            ClassTypeDescriptor declaredIn = store.find(ClassTypeDescriptor.class, value);
                            testCaseDescriptor.setDeclaredIn(declaredIn);
                        }
                    }
                } else if ("failure".equals(elementName)) {
                    testCaseDescriptor.setResult(TestCaseDescriptor.Result.FAILURE);
                } else if ("error".equals(elementName)) {
                    testCaseDescriptor.setResult(TestCaseDescriptor.Result.ERROR);
                } else if ("skipped".equals(elementName)) {
                    testCaseDescriptor.setResult(TestCaseDescriptor.Result.SKIPPED);
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
