package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContainerFileResolverIT extends AbstractPluginIT {

    @Test
    public void resolve() {
        store.beginTransaction();
        DirectoryDescriptor container = store.create(DirectoryDescriptor.class);
        container.setFileName("/");
        ContainerFileResolver resolver = new ContainerFileResolver(container);

        FileDescriptor required1 = resolver.require("/file", FileDescriptor.class, getScanner().getContext());
        assertThat(required1.getFileName()).isEqualTo("/file");

        FileDescriptor required2 = resolver.require("/file", FileDescriptor.class, getScanner().getContext());
        assertThat(required2).isSameAs(required1);

        resolver.flush();
        store.commitTransaction();

    }

}
