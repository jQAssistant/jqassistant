package com.buschmais.jqassistant.plugin.common.test.mapper;

import com.buschmais.jqassistant.plugin.common.api.mapper.DescriptorMapper;

import org.mapstruct.Mapper;

@Mapper
public interface ModelMapper extends DescriptorMapper<Model, ModelDescriptor> {
}
