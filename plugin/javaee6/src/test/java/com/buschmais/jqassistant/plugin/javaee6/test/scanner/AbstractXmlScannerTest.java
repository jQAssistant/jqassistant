package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.mockito.Mock;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.javaee6.api.model.*;

public abstract class AbstractXmlScannerTest {

    @Mock
    protected Scanner scanner;

    @Mock
    protected ScannerContext scannerContext;

    @Mock
    protected Store store;

    @Before
    public void before() {
        when(scanner.getContext()).thenReturn(scannerContext);
        when(scannerContext.getStore()).thenReturn(store);
    }

    protected void verifyIcon(List<IconDescriptor> descriptors, IconDescriptor descriptor, String smallIcon, String largeIcon) {
        verify(descriptors).add(descriptor);
        verify(descriptor).setSmallIcon(smallIcon);
        verify(descriptor).setLargeIcon(largeIcon);
    }

    protected void verifyDisplayName(List<DisplayNameDescriptor> descriptors, DisplayNameDescriptor descriptor, String lang, String value) {
        verify(descriptors).add(descriptor);
        verify(descriptor).setLang(lang);
        verify(descriptor).setValue(value);
    }

    protected void verifyDescription(List<DescriptionDescriptor> descriptors, DescriptionDescriptor descriptor, String lang, String value) {
        verify(descriptors).add(descriptor);
        verify(descriptor).setLang(lang);
        verify(descriptor).setValue(value);
    }

    protected void verifySecurityRole(List<SecurityRoleDescriptor> securityRoleDescriptors, SecurityRoleDescriptor securityRoleDescriptor,
            DescriptionDescriptor securityRoleDescriptionDescriptor, RoleNameDescriptor roleNameDescriptor, String lang, String description, String roleName) {
        verify(store).create(SecurityRoleDescriptor.class);
        verify(securityRoleDescriptors).add(securityRoleDescriptor);
        verifyDescription(securityRoleDescriptor.getDescriptions(), securityRoleDescriptionDescriptor, lang, description);
        verify(securityRoleDescriptor).setRoleName(roleNameDescriptor);
        verify(roleNameDescriptor).setName(roleName);
    }
}