package com.buschmais.jqassistant.plugin.xml.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlDocumentDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlElementDescriptor;
import com.buschmais.jqassistant.plugin.xml.impl.scanner.XmlSourceScannerPlugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class XmlSourceScannerPluginTest {

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext scannerContext;

    @Mock
    private XmlDocumentDescriptor documentDescriptor;

    @Mock
    private Store store;

    /**
     * Contains created {@link XmlElementDescriptor}s and their parents.
     */
    private Map<XmlElementDescriptor, XmlElementDescriptor> parents = new HashMap<>();

    @Before
    public void setUp() {
        doReturn(scannerContext).when(scanner).getContext();
        doReturn(documentDescriptor).when(scannerContext).peek(XmlDocumentDescriptor.class);
        doReturn(store).when(scannerContext).getStore();
        doAnswer(invocation -> {
            Class<? extends XmlDescriptor> descriptor = (Class<? extends XmlDescriptor>) invocation.getArguments()[0];
            XmlDescriptor xmlDescriptor = mock(descriptor);
            if (xmlDescriptor instanceof XmlElementDescriptor) {
                XmlElementDescriptor xmlElementDescriptor = (XmlElementDescriptor) xmlDescriptor;
                stubXmlElementDescriptor(xmlElementDescriptor);
            }
            return xmlDescriptor;
        }).when(store).create(any());

    }

    private void stubXmlElementDescriptor(XmlElementDescriptor xmlElementDescriptor) {
        doAnswer(invocation -> {
            parents.put(xmlElementDescriptor, (XmlElementDescriptor) invocation.getArguments()[0]);
            return null;
        }).when(xmlElementDescriptor).setParent(any(XmlElementDescriptor.class));
        doAnswer(invocation -> parents.get(xmlElementDescriptor)).when(xmlElementDescriptor).getParent();
    }

    @Test
    public void parentRelation() throws IOException {
        XmlSourceScannerPlugin scannerPlugin = new XmlSourceScannerPlugin();
        scannerPlugin.initialize();
        XmlDocumentDescriptor documentDescriptor = scannerPlugin.scan(
                new StreamSource(new StringReader(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<RootElement xmlns=\"http://jqassistant.org/plugin/xml/test/root\">\n"
                                + "    <ChildElement attribute1=\"attribute1\">\n" + "        Child Text\n" + "    </ChildElement>\n" + "</RootElement>")),
                "test.xml", DefaultScope.NONE, scanner);

        assertThat(documentDescriptor, notNullValue());
        assertThat(parents.size(), equalTo(1));
        Map.Entry<XmlElementDescriptor, XmlElementDescriptor> entry = parents.entrySet().iterator().next();
        XmlElementDescriptor childElement = entry.getKey();
        XmlElementDescriptor rootElement = entry.getValue();
        verify(childElement).setName("ChildElement");
        verify(rootElement).setName("RootElement");
        verify(childElement).setParent(rootElement);
    }
}
