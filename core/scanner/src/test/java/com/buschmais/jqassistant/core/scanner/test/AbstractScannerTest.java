package com.buschmais.jqassistant.core.scanner.test;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.core.scanner.impl.ClassScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractScannerTest {

    @Mock
    protected Store store;

    protected ClassScannerImpl scanner;

    private final Map<String, PackageDescriptor> packageCache = new HashMap<String, PackageDescriptor>();

    protected TypeDescriptor javaLangObject;
    protected TypeDescriptor _void;

    @Before
    public void createScanner() {
		scanner = new ClassScannerImpl(store);
        this.javaLangObject = stubClass(Object.class);
        this._void = stubClass("void");
    }

    protected PackageDescriptor stubPackage(String fullQualifiedName) {
        if (fullQualifiedName == null) {
            return null;
        }
        PackageDescriptor packageDescriptor = packageCache.get(fullQualifiedName);
        if (packageDescriptor == null) {
            int i = fullQualifiedName.lastIndexOf('.');
            PackageDescriptor parentDescriptor = null;
            String name;
            if (i != -1) {
                String parentName = fullQualifiedName.substring(0, i);
                name = fullQualifiedName.substring(i + 1, fullQualifiedName.length());
                parentDescriptor = stubPackage(parentName);
            } else {
                name = fullQualifiedName;
            }
            packageDescriptor = new PackageDescriptor();
            packageDescriptor.setFullQualifiedName(fullQualifiedName);
            when(store.createPackageDescriptor(parentDescriptor, name)).thenReturn(packageDescriptor);
            packageCache.put(fullQualifiedName, packageDescriptor);
        }
        return packageDescriptor;
    }

    protected TypeDescriptor stubClass(Class<?> c) {
        return stubClass(stubPackage(c.getPackage().getName()), c.getSimpleName());
    }

    protected TypeDescriptor stubClass(String className) {
        return stubClass((String) null, className);
    }

    protected TypeDescriptor stubClass(String packageName, String className) {
        return stubClass(stubPackage(packageName), className);
    }

    protected TypeDescriptor stubClass(PackageDescriptor packageDescriptor, String className) {
        TypeDescriptor typeDescriptor = new TypeDescriptor();
        if (packageDescriptor != null) {
            typeDescriptor.setFullQualifiedName(packageDescriptor.getFullQualifiedName() + "." + className);
        } else {
            typeDescriptor.setFullQualifiedName(className);
        }
        when(store.createClassDescriptor(packageDescriptor, className)).thenReturn(typeDescriptor);
        return typeDescriptor;
    }
}
