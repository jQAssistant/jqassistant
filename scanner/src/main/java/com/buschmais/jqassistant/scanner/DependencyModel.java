package com.buschmais.jqassistant.scanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.store.model.ClassDescriptor;
import com.buschmais.jqassistant.store.model.PackageDescriptor;

public class DependencyModel {

    private final Map<String, ClassDescriptor> classDescriptors = new HashMap<String, ClassDescriptor>();

    private final Map<String, PackageDescriptor> packageDescriptors = new HashMap<String, PackageDescriptor>();

    private final Map<ClassDescriptor, Set<ClassDescriptor>> dependencies = new HashMap<ClassDescriptor, Set<ClassDescriptor>>();

    public void addDependency(ClassDescriptor classDescriptor, final String name) {
        if (name == null) {
            return;
        }
        ClassDescriptor p = getClassDescriptor(name);
        Set<ClassDescriptor> dependsOn = this.dependencies.get(classDescriptor);
        if (dependsOn == null) {
            dependsOn = new HashSet<ClassDescriptor>();
            this.dependencies.put(classDescriptor, dependsOn);
        }
        dependsOn.add(p);
    }

    public ClassDescriptor getClassDescriptor(String name) {
        ClassDescriptor classDescriptor = classDescriptors.get(name);
        if (classDescriptor == null) {
            String fullQualifiedName = name.replace("/", ".");
            int n = fullQualifiedName.lastIndexOf('.');
            String packageName;
            String className;
            if (n > -1) {
                packageName = fullQualifiedName.substring(0, n);
                className = fullQualifiedName.substring(n + 1, fullQualifiedName.length());
            } else {
                className = fullQualifiedName;
                packageName = "";
            }
            PackageDescriptor packageDescriptor = getPackageDescriptor(packageName);
            classDescriptor = new ClassDescriptor(packageDescriptor, className);
            classDescriptors.put(className, classDescriptor);
        }
        return classDescriptor;
    }

    public PackageDescriptor getPackageDescriptor(String packageName) {
        PackageDescriptor packageDescriptor = packageDescriptors.get(packageName);
        if (packageDescriptor == null) {
            int n = packageName.lastIndexOf('.');
            PackageDescriptor parent = null;
            String localName;
            if (n > -1) {
                parent = getPackageDescriptor(packageName.substring(0, n));
                localName = packageName.substring(n + 1, packageName.length());
            } else {
                localName = packageName;
            }
            packageDescriptor = new PackageDescriptor(parent, localName);
            this.packageDescriptors.put(packageName, packageDescriptor);
        }
        return packageDescriptor;
    }

    public Map<ClassDescriptor, Set<ClassDescriptor>> getDependencies() {
        return dependencies;
    }

}
