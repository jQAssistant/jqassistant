package com.buschmais.jqassistant.core.runtime.api.configuration;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class JsonSchemaGenerator {

    public ObjectNode generateSchema(Class<?> clazz) {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.JAVA_OBJECT)
            .with(Option.MAP_VALUES_AS_ADDITIONAL_PROPERTIES, Option.NONSTATIC_NONVOID_NONGETTER_METHODS,
                Option.FIELDS_DERIVED_FROM_ARGUMENTFREE_METHODS, Option.FLATTENED_ENUMS, Option.ALLOF_CLEANUP_AT_THE_END, Option.ALLOF_CLEANUP_AT_THE_END).without(Option.VOID_METHODS, Option.GETTER_METHODS);

        configBuilder.forMethods()
            .withTargetTypeOverridesResolver(target -> {
                ResolvedType resolvedType = target.getType();
                return getResolvedTypes(target, resolvedType);
            });

        configBuilder.forFields()
            .withIgnoreCheck(field -> field.getName().startsWith("PREFIX") || field.getName().startsWith("SKIP"));
        configBuilder.forMethods().withPropertyNameOverrideResolver((member) -> mapToKebabCase(member.getName()));
        configBuilder.forFields().withPropertyNameOverrideResolver((member) -> mapToKebabCase(member.getName()));

        configBuilder.forTypesInGeneral()
            .withCustomDefinitionProvider((javaType, context) -> {
                if (javaType.isInstanceOf(Map.class)) {
                    ObjectNode mapNode = context.getGeneratorConfig().createObjectNode();
                    mapNode.put("type", "object");
                    StringBuilder types = null;
                    for (int i = 0; i < javaType.getTypeBindings().size(); i++) {
                        if (types == null) {
                            types = new StringBuilder(javaType.getTypeBindings().getBoundType(i).getErasedType().getSimpleName().toLowerCase());
                        }else{
                            types.append(", ").append(javaType.getTypeBindings().getBoundType(i).getErasedType().getSimpleName().toLowerCase());
                        }
                    }
                    ObjectNode addNode = context.getGeneratorConfig().createObjectNode();
                    addNode.put("type", types.toString());
                    mapNode.set("additionalProperties", addNode);
                    return new CustomDefinition(mapNode);
            }
        return null;
    });

    SchemaGenerator generator = new SchemaGenerator(configBuilder.build());
    ObjectNode jsonSchema = generator.generateSchema(clazz);

    saveSchemaToFile(jsonSchema, "target","jsonSchema.json");
        return jsonSchema;
}

private static List<ResolvedType> getResolvedTypes(MethodScope target, ResolvedType resolvedType) {
    if (resolvedType != null) {
        if (resolvedType.getErasedType().equals(URI.class)) {
            return List.of(target.getContext().resolve(String.class));
        }
        if (resolvedType.getErasedType().equals(Map.class) && resolvedType.getErasedType().getComponentType() != null) {
            return List.of(target.getContext().resolve(Map.class));
        }
        if (resolvedType.getErasedType().equals(Optional.class)) {
            return getResolvedTypes(target, resolvedType.getTypeParameters().get(0));
        }
    }
    return List.of(target.getContext().resolve(resolvedType));
}

private void saveSchemaToFile(ObjectNode schema, String targetFolder, String fileName) {
    ObjectMapper objectMapper = new ObjectMapper();
    File targetFile = new File(targetFolder, fileName);
    try {
        new File(targetFolder);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(targetFile, schema);
        System.out.println("Schema saved: " + targetFile.getAbsolutePath());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private static String mapToKebabCase(String name) {
    if (name.matches("^[A-Z0-9_]+$")) {
        return name.toLowerCase().replace("_", "-");
    }
    return name.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
}

}
