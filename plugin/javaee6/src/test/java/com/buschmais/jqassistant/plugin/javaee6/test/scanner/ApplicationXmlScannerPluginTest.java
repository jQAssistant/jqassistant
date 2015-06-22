package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.javaee6.api.model.ApplicationXmlDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.ClientModuleDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.ConnectorModuleDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.DescriptionDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.DisplayNameDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.EjbModuleDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.IconDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.RoleNameDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.SecurityRoleDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebModuleDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.EnterpriseApplicationScope;
import com.buschmais.jqassistant.plugin.javaee6.impl.scanner.ApplicationXmlScannerPlugin;
import com.buschmais.jqassistant.plugin.javaee6.impl.scanner.WebXmlScannerPlugin;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XmlScope;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationXmlScannerPluginTest extends AbstractXmlScannerTest {

    @Mock
    private ApplicationXmlDescriptor applicationXmlDescriptor;

    @Mock
    private DescriptionDescriptor appDescriptionDescriptor;

    @Mock
    private DisplayNameDescriptor appDisplayNameDescriptor;

    @Mock
    private IconDescriptor appIconDescriptor;

    @Mock
    private EjbModuleDescriptor ejbModuleDescriptor;

    @Mock
    private WebModuleDescriptor webModuleDescriptor;

    @Mock
    private ConnectorModuleDescriptor connectorModuleDescriptor;

    @Mock
    private ClientModuleDescriptor clientModuleDescriptor;

    @Mock
    private SecurityRoleDescriptor securityRoleDescriptor;

    @Mock
    private RoleNameDescriptor roleNameDescriptor;

    @Mock
    private DescriptionDescriptor securityRoleDescriptionDescriptor;

    @Test
    public void applicationXml() throws IOException {

        FileResource fileResource = mock(FileResource.class);
        when(fileResource.createStream()).thenReturn(WebXmlScannerPlugin.class.getResourceAsStream("/META-INF/application.xml"));

        when(scanner.scan(fileResource, "/META-INF/application.xml", XmlScope.DOCUMENT)).thenReturn(applicationXmlDescriptor);
        when(store.addDescriptorType(applicationXmlDescriptor, ApplicationXmlDescriptor.class)).thenReturn(applicationXmlDescriptor);
        when(applicationXmlDescriptor.getDescriptions()).thenReturn(mock(List.class));
        when(applicationXmlDescriptor.getDisplayNames()).thenReturn(mock(List.class));
        when(applicationXmlDescriptor.getIcons()).thenReturn(mock(List.class));
        when(applicationXmlDescriptor.getSecurityRoles()).thenReturn(mock(List.class));
        when(applicationXmlDescriptor.getModules()).thenReturn(mock(List.class));

        when(store.create(DisplayNameDescriptor.class)).thenReturn(appDisplayNameDescriptor);
        when(store.create(DescriptionDescriptor.class)).thenReturn(appDescriptionDescriptor, securityRoleDescriptionDescriptor);
        when(store.create(IconDescriptor.class)).thenReturn(appIconDescriptor);

        when(store.create(EjbModuleDescriptor.class)).thenReturn(ejbModuleDescriptor);
        when(store.create(WebModuleDescriptor.class)).thenReturn(webModuleDescriptor);
        when(store.create(ConnectorModuleDescriptor.class)).thenReturn(connectorModuleDescriptor);
        when(store.create(ClientModuleDescriptor.class)).thenReturn(clientModuleDescriptor);

        // Security Role
        when(store.create(SecurityRoleDescriptor.class)).thenReturn(securityRoleDescriptor);
        when(securityRoleDescriptor.getDescriptions()).thenReturn(mock(List.class));
        when(store.create(RoleNameDescriptor.class)).thenReturn(roleNameDescriptor, null);

        ApplicationXmlScannerPlugin scannerPlugin = new ApplicationXmlScannerPlugin();
        scannerPlugin.initialize();
        scannerPlugin.configure(scannerContext, Collections.<String, Object>emptyMap());
        scannerPlugin.scan(fileResource, "/META-INF/application.xml", EnterpriseApplicationScope.EAR, scanner);

        verify(scanner).scan(fileResource, "/META-INF/application.xml", XmlScope.DOCUMENT);
        verify(store).addDescriptorType(applicationXmlDescriptor, ApplicationXmlDescriptor.class);
        verify(applicationXmlDescriptor).setVersion("6");
        verify(applicationXmlDescriptor).setName("TestApplication");
        verify(applicationXmlDescriptor).setInitializeInOrder("true");
        verify(applicationXmlDescriptor).setLibraryDirectory("lib");

        verifyDescription(applicationXmlDescriptor.getDescriptions(), appDescriptionDescriptor, "en", "Test Application Description");
        verifyDisplayName(applicationXmlDescriptor.getDisplayNames(), appDisplayNameDescriptor, "en", "Test Application");
        verifyIcon(applicationXmlDescriptor.getIcons(), appIconDescriptor, "icon-small.png", "icon-large.png");

        verify(store).create(EjbModuleDescriptor.class);
        verify(ejbModuleDescriptor).setPath("ejbModule.jar");
        verify(store).create(WebModuleDescriptor.class);
        verify(webModuleDescriptor).setPath("webModule.war");
        verify(store).create(ConnectorModuleDescriptor.class);
        verify(connectorModuleDescriptor).setPath("connectorModule.rar");
        verify(store).create(ClientModuleDescriptor.class);
        verify(clientModuleDescriptor).setPath("javaModule.jar");

        verifySecurityRole(applicationXmlDescriptor.getSecurityRoles(), securityRoleDescriptor, securityRoleDescriptionDescriptor, roleNameDescriptor, "en",
                "Admin Role", "Admin");
    }

}
