package com.buschmais.jqassistant.plugin.junit4.impl.scanner;

import static com.buschmais.jqassistant.plugin.junit4.api.JunitScope.TESTREPORTS;
import static java.util.Arrays.asList;

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
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.junit4.impl.store.descriptor.TestCaseDescriptor;
import com.buschmais.jqassistant.plugin.junit4.impl.store.descriptor.TestSuiteDescriptor;

public class TestReportScannerPlugin extends AbstractScannerPlugin<InputStream> {

    private final NumberFormat timeFormat = NumberFormat.getInstance(Locale.US);

    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super InputStream> getType() {
        return InputStream.class;
    }

    @Override
    public boolean accepts(InputStream item, String path, Scope scope) throws IOException {
        return TESTREPORTS.equals(scope) && path.matches(".*TEST-.*\\.xml");
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(InputStream item, String path, Scope scope, Scanner scanner) throws IOException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader;
        try {
            reader = inputFactory.createXMLEventReader(item);
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
                @SuppressWarnings("unchecked")
                Iterator<Attribute> attributes = element.getAttributes();
                switch (elementName) {
                case "testsuite":
                    testSuiteDescriptor = getStore().create(TestSuiteDescriptor.class);
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
                    testCaseDescriptor = getStore().create(TestCaseDescriptor.class);
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
        testSuiteDescriptor.setFileName(path);
        return asList(testSuiteDescriptor);
    }

    private float parseTime(String value) throws IOException {
        try {
            return timeFormat.parse(value).floatValue();
        } catch (ParseException e) {
            throw new IOException("Cannot parse time.", e);
        }
    }
}
