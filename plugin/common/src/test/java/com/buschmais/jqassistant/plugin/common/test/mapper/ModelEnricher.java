package com.buschmais.jqassistant.plugin.common.test.mapper;

import com.buschmais.jqassistant.plugin.common.api.mapper.DescriptorEnricher;

import org.mapstruct.Mapper;

@Mapper
public interface ModelEnricher extends DescriptorEnricher<Model, ModelDescriptor> {
}
