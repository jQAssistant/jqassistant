package com.buschmais.jqassistant.plugin.common.test.mapper;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;

import static org.mapstruct.factory.Mappers.getMapper;

public class ModelScannerPlugin extends AbstractScannerPlugin<Model, ModelDescriptor> {

    @Override
    public boolean accepts(Model item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public ModelDescriptor scan(Model model, String path, Scope scope, Scanner scanner) throws IOException {
        Descriptor currentDescriptor = scanner.getContext()
            .getCurrentDescriptor();
        if (currentDescriptor != null) {
            ModelDescriptor modelDescriptor = scanner.getContext()
                .getStore()
                .addDescriptorType(currentDescriptor, ModelDescriptor.class);
            return getMapper(ModelEnricher.class).toDescriptor(model, modelDescriptor, scanner);
        } else {
            return getMapper(ModelMapper.class).toDescriptor(model, scanner);
        }
    }
}
