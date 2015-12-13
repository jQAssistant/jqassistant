package com.buschmais.jqassistant.plugin.jpa2.impl.scanner;

import com.buschmais.jqassistant.core.analysis.api.rule.source.FileRuleSourceTest;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.jpa2.api.model.PersistenceUnitDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.api.model.PersistenceXmlDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XmlScope;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceXmlScannerPluginTest {

    private static PersistenceXmlScannerPlugin plugin;

    @Mock
    TypeResolver typeResolver;

    @Mock
    FileResource item4V20;

    @Mock
    FileResource itemMinimal4V20;

    @Mock
    FileResource itemMinimal4V21;

    @Mock
    FileResource item4V21;

    @Mock
    Store store;

    @Mock
    ScannerContext context;

    @Mock
    XmlFileDescriptor xmlFileDescriptor;

    @Mock
    TypeDescriptor jpaEntityDescriptor;

    @Mock
    TypeCache.CachedType<TypeDescriptor> cachedType;

    @Mock
    Scanner scanner;

    @Mock
    PersistenceXmlDescriptor persistenceDescriptor;

    @Spy
    List<PersistenceUnitDescriptor> persistenceUnitList = new LinkedList<>();

    @Spy
    List<TypeDescriptor> persistenceEntities = new LinkedList<>();

    @Mock
    PersistenceUnitDescriptor unitDescriptor;

    @Mock
    PropertyDescriptor propertyDescriptor;

    @Spy
    Set<PropertyDescriptor> properties = new HashSet<PropertyDescriptor>();

    private String path = "/META-INF/persistence.xml";

    @Before
    public void createScanner() {
        plugin = new PersistenceXmlScannerPlugin();
        plugin.initialize();
    }

    @Before
    public void configureMocks() throws IOException {
        doReturn(persistenceEntities).when(unitDescriptor).getContains();
        doReturn(store).when(context).getStore();

        doAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocationOnMock) throws Throwable {
                return PersistenceXmlScannerPluginTest.class.getResourceAsStream("/v2dot0/full/META-INF/persistence.xml");
            }
        }).when(item4V20).createStream();

        doAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocationOnMock) throws Throwable {
                return PersistenceXmlScannerPluginTest.class.getResourceAsStream("/v2dot1/full/META-INF/persistence.xml");
            }
        }).when(item4V21).createStream();


        doAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocationOnMock) throws Throwable {
                return PersistenceXmlScannerPluginTest.class.getResourceAsStream("/v2dot0/minimal/META-INF/persistence.xml");
            }
        }).when(itemMinimal4V20).createStream();

        doAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocationOnMock) throws Throwable {
                return PersistenceXmlScannerPluginTest.class.getResourceAsStream("/2_1/minimal/META-INF/persistence.xml");
            }
        }).when(itemMinimal4V21).createStream();


        doReturn(properties).when(unitDescriptor).getProperties();
        doReturn(propertyDescriptor).when(store).create(PropertyDescriptor.class);
        doReturn(jpaEntityDescriptor).when(cachedType).getTypeDescriptor();
        doReturn(cachedType).when(typeResolver).resolve(eq("com.buschmais.jqassistant.plugin.jpa2.test.set.entity.JpaEntity"),
                eq(context));
        doReturn(typeResolver).when(context).peek(TypeResolver.class);
        doReturn(context).when(scanner).getContext();
        doReturn(xmlFileDescriptor).when(scanner).scan(eq(item4V20), eq(path), eq(XmlScope.DOCUMENT));
        doReturn(xmlFileDescriptor).when(scanner).scan(eq(item4V21), eq(path), eq(XmlScope.DOCUMENT));
        doReturn(xmlFileDescriptor).when(scanner).scan(eq(itemMinimal4V20), eq(path), eq(XmlScope.DOCUMENT));
        doReturn(xmlFileDescriptor).when(scanner).scan(eq(itemMinimal4V21), eq(path), eq(XmlScope.DOCUMENT));
        doReturn(persistenceDescriptor).when(store).addDescriptorType(xmlFileDescriptor, PersistenceXmlDescriptor.class);
        doReturn(persistenceUnitList).when(persistenceDescriptor).getContains();
        doReturn(unitDescriptor).when(store).create(PersistenceUnitDescriptor.class);
    }

    @Test
    public void scannerAcceptsIfInClasspathScope() throws IOException {
        FileResource item = Mockito.mock(FileResource.class, new FileRuleSourceTest.MethodNotMockedAnswer());
        String path = "/META-INF/persistence.xml";
        Scope scope = JavaScope.CLASSPATH;

        assertThat(plugin.accepts(item, path, scope), is(true));
    }

    @Test
    public void scannerAcceptsIfPersistenceXMLIsInMETAINF() throws Exception {
        FileResource item = Mockito.mock(FileResource.class, new FileRuleSourceTest.MethodNotMockedAnswer());
        String path = "/META-INF/persistence.xml";
        Scope scope = JavaScope.CLASSPATH;

        assertThat(plugin.accepts(item, path, scope), is(true));
    }


    @Test
    public void scannerAcceptsIfPersistenceXMLIsInWEBINF() throws Exception {
        FileResource item = Mockito.mock(FileResource.class, new FileRuleSourceTest.MethodNotMockedAnswer());
        String path = "/WEB-INF/persistence.xml";
        Scope scope = JavaScope.CLASSPATH;

        assertThat(plugin.accepts(item, path, scope), is(true));
    }

    @Test
    public void scannerFindAllPropertisInPersistenceXMLV20() throws IOException {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        Mockito.verify(store, times(1)).create(PropertyDescriptor.class);
        Mockito.verify(propertyDescriptor, times(1)).setValue("stringValue");
        Mockito.verify(propertyDescriptor, times(1)).setName("stringProperty");
        Mockito.verify(properties).add(eq(propertyDescriptor));
    }

    @Test
    public void scannerFindAllPropertisInPersistenceXMLV21() throws IOException {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        Mockito.verify(store, times(1)).create(PropertyDescriptor.class);
        Mockito.verify(propertyDescriptor, times(1)).setValue("stringValue");
        Mockito.verify(propertyDescriptor, times(1)).setName("stringProperty");
        Mockito.verify(properties).add(eq(propertyDescriptor));
    }

    @Test
    public void scannerFindVersionInPersistenceXMLV20() throws IOException {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        Mockito.verify(persistenceDescriptor).setVersion(eq("2.0"));
    }

    @Test
    public void scannerFindVersionInPersistenceXMLV21() throws IOException {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        Mockito.verify(persistenceDescriptor).setVersion(eq("2.1"));
    }

    @Test
    public void scannerFindsOnePersistenceUnitInPersistenceXMLV20() throws IOException {

        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        verify(persistenceUnitList).add(Mockito.any(PersistenceUnitDescriptor.class));
    }

    @Test
    public void scannerFindsOnePersistenceUnitInPersistenceXMLV21() throws IOException {

        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        verify(persistenceUnitList).add(Mockito.any(PersistenceUnitDescriptor.class));
    }

    @Test
    public void scannerSetsCorrectNameForPersistenceUnitInPersistenceXMLV20() throws IOException {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be on persistence unit.", persistenceUnitList, hasSize(1));

        verify(persistenceUnitList.get(0)).setName(eq("persistence-unit"));
    }

    @Test
    public void scannerSetsCorrectNameForPersistenceUnitInPersistenceXMLV21() throws IOException {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be on persistence unit.", persistenceUnitList, hasSize(1));

        verify(persistenceUnitList.get(0)).setName(eq("unit21"));
    }

    @Test
    public void scannerSetcCorrectTransactionTypeForPersistenceUnitInPersistenceXMLV20() throws IOException {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        assertThat(persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setTransactionType(eq("RESOURCE_LOCAL"));
    }

    @Test
    public void scannerSetcCorrectTransactionTypeForPersistenceUnitInPersistenceXMLV21() throws IOException {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        assertThat(persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setTransactionType(eq("JTA"));
    }

    @Test
    public void scannerSetsCorrectDescriptionFoundInPersistenceUnitInPersistenceXMLV20() throws IOException {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be one persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setDescription(eq("description"));
    }

    @Test
    public void scannerSetsCorrectDescriptionFoundInPersistenceUnitInPersistenceXMLV21() throws IOException {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be one persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setDescription(eq("bla"));
    }

    @Test
    public void scannerSetsCorrectJTADataSourceFromPersistenceUnitInPersistenceXMLV20() throws IOException {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be one persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setJtaDataSource(eq("jtaDataSource"));
    }

    @Test
    public void scannerSetsCorrectJTADataSourceFromPersistenceUnitInPersistenceXMLV21() throws IOException {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be one persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setJtaDataSource(eq("jtaDataSource"));
    }

    @Test
    public void scannerSetsCorrectNonJTADataSourceFromPersistenceUnitInPersistenceXMLV20() throws IOException {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be one persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setNonJtaDataSource(eq("nonJtaDataSource"));
    }

    @Test
    public void scannerSetsCorrectNonJTADataSourceFromPersistenceUnitInPersistenceXMLV21() throws IOException {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be one persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setNonJtaDataSource(eq("nonJtaDataSource"));
    }

    @Test
    public void scannerSetsCorrectProviderFromPersistenceUnitInPersistenceXMLV20() throws IOException {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be one persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setProvider(eq("provider"));
    }

    @Test
    public void scannerSetsCorrectProviderFromPersistenceUnitInPersistenceXMLV21() throws IOException {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be one persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setProvider(eq("other"));
    }

    @Test
    public void scannerSetsCorrectValidationModeFromPersistenceUnitInPersistenceXMLV20() throws IOException {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be one persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setValidationMode(eq("AUTO"));
    }

    @Test
    public void scannerSetsCorrectValidationModeFromPersistenceUnitInPersistenceXMLV21() throws IOException {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be one persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setValidationMode(eq("NONE"));
    }

    @Test
    public void scannerSetsCorrectSharedCacheModeFromPersistenceUnitInPersistenceXMLV20() throws IOException {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        assertThat(persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setValidationMode(eq("AUTO"));
    }

    @Test
    public void scannerSetsCorrectSharedCacheModeFromPersistenceUnitInPersistenceXMLV21() throws IOException {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        assertThat(persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setValidationMode(eq("NONE"));
    }


    @Test
    public void scannerAddsAllClasseseFromPersistenceUnitInPersistenceXMLV20() throws IOException {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be unit persistence unit.", persistenceUnitList, hasSize(1));
        assertThat("There must be one JPA entity class.", persistenceUnitList.get(0).getContains(), hasSize(1));
        assertThat(persistenceUnitList.get(0).getContains(), hasItem(equalTo(cachedType.getTypeDescriptor())));
    }

    @Test
    public void scannerAddsAllClasseseFromPersistenceUnitInPersistenceXMLV21() throws IOException {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be unit persistence unit.", persistenceUnitList, hasSize(1));
        assertThat("There must be one JPA entity class.", persistenceUnitList.get(0).getContains(), hasSize(1));
        assertThat(persistenceUnitList.get(0).getContains(), hasItem(equalTo(cachedType.getTypeDescriptor())));
    }

    @Test
    public void scannerSetsExcludeUnlistedClassesToTrueIfNotSpecifiedXMLV20() throws Exception {
        plugin.scan(itemMinimal4V20, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be unit persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setExcludingUnlistedClasses(eq(true));
    }

    @Test
    public void scannerSetsExcludeUnlistedClassesToTrueIfNotSpecifiedXMLV21() throws Exception {
        plugin.scan(itemMinimal4V21, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be unit persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setExcludingUnlistedClasses(eq(true));
    }

    @Test
    public void scannerSetsExcludeUnlistedClassesAsSpecifiedXMLV20() throws Exception {
        plugin.scan(item4V20, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be unit persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setExcludingUnlistedClasses(eq(false));
    }

    @Test
    public void scannerSetsExcludeUnlistedClassesAsSpecifiedXMLV21() throws Exception {
        plugin.scan(item4V21, path, JavaScope.CLASSPATH, scanner);

        assertThat("There must be unit persistence unit.", persistenceUnitList, hasSize(1));
        verify(persistenceUnitList.get(0)).setExcludingUnlistedClasses(eq(false));
    }
}