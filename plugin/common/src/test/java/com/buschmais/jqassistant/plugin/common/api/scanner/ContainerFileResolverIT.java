package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContainerFileResolverIT extends com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT {

    @Test
    public void requireFile() {
        store.beginTransaction();
        DirectoryDescriptor container = store.create(DirectoryDescriptor.class);
        container.setFileName("/");
        ContainerFileResolver resolver = new ContainerFileResolver(getScanner().getContext(), container);

        FileDescriptor required1 = resolver.require("/file", FileDescriptor.class, getScanner().getContext());
        assertThat(required1.getFileName()).isEqualTo("/file");

        FileDescriptor required2 = resolver.require("/file", FileDescriptor.class, getScanner().getContext());
        assertThat(required2).isSameAs(required1);

        resolver.flush();
        store.commitTransaction();
    }

    @Test
    public void nestedContainer() {
        store.beginTransaction();
        DirectoryDescriptor parent = store.create(DirectoryDescriptor.class);
        parent.setFileName("/parentContainer");
        ContainerFileResolver parentResolver = new ContainerFileResolver(getScanner().getContext(), parent);

        FileDescriptor parentFile = parentResolver.require("/file", FileDescriptor.class, getScanner().getContext());
        assertThat(parentFile.getFileName()).isEqualTo("/file");

        DirectoryDescriptor child = parentResolver.require("/childContainer", DirectoryDescriptor.class, getScanner().getContext());
        assertThat(child.getFileName()).isEqualTo("/childContainer");

        ContainerFileResolver childResolver = new ContainerFileResolver(getScanner().getContext(), child);

        FileDescriptor childFile = childResolver.require("/file", FileDescriptor.class, getScanner().getContext());

        assertThat(parentFile).isNotSameAs(childFile);

        childResolver.flush();

        parentResolver.flush();
        store.commitTransaction();
    }
}
