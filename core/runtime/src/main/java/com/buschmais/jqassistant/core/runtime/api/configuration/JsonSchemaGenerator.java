package com.buschmais.jqassistant.core.runtime.api.configuration;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import io.smallrye.config.WithDefault;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonSchemaGenerator {

    public ObjectNode generateSchema(Class<?> clazz) {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.JAVA_OBJECT)
            .with(Option.MAP_VALUES_AS_ADDITIONAL_PROPERTIES, Option.NONSTATIC_NONVOID_NONGETTER_METHODS, Option.PLAIN_DEFINITION_KEYS,
                Option.FIELDS_DERIVED_FROM_ARGUMENTFREE_METHODS, Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT, Option.FLATTENED_ENUMS, Option.ALLOF_CLEANUP_AT_THE_END).without(Option.VOID_METHODS, Option.GETTER_METHODS);

        configBuilder.forMethods()
            .withTargetTypeOverridesResolver(target -> {
                ResolvedType resolvedType = target.getType();
                return getResolvedTypes(target, resolvedType);
            });
        configBuilder.forFields()
            .withIgnoreCheck(field -> field.getName().startsWith("PREFIX") || field.getName().startsWith("SKIP") || field.getName().startsWith("DEFAULT") );

        configBuilder.forMethods().withPropertyNameOverrideResolver((member) -> mapToKebabCase(member.getName()));
        configBuilder.forFields().withPropertyNameOverrideResolver((member) -> mapToKebabCase(member.getName()));
        configBuilder.forTypesInGeneral()
            .withIdResolver(scope -> scope.getType().getErasedType().isInstance(Configuration.class) ? "core/runtime/target/test-classes/jsonSchema.json" : null);
        configBuilder.forTypesInGeneral()
            .withCustomDefinitionProvider((javaType, context) -> {
                if (javaType.isInstanceOf(Map.class)) {
                    ObjectNode mapNode = context.getGeneratorConfig().createObjectNode();
                    mapNode.put("type", "object");
                    ObjectNode addNode = context.getGeneratorConfig().createObjectNode();
                    addNode.withArrayProperty("type");
                    addNode.put("type", "string");
                    mapNode.set("additionalProperties", addNode);
                    return new CustomDefinition(mapNode);
                }
                return null;
            });
        configBuilder.forMethods().withDefaultResolver(field -> {
            WithDefault annotation = field.getAnnotationConsideringFieldAndGetter(WithDefault.class);
            if (annotation != null) {
                return annotation.value();
            }
            return null;
        });
        configBuilder.forTypesInGeneral()
            .withDefinitionNamingStrategy((definitionKey, context) -> mapToKebabCase(definitionKey.getType().getTypeName()));

        SchemaGenerator generator = new SchemaGenerator(configBuilder.build());
        return generator.generateSchema(clazz);
    }

    private static List<ResolvedType> getResolvedTypes(MethodScope target, ResolvedType resolvedType) {
        if (resolvedType != null) {
            if (resolvedType.getErasedType().equals(URI.class)) {
                return List.of(target.getContext().resolve(String.class));
            }
            if (resolvedType.isInstanceOf(Map.class)) {
                return List.of(target.getContext().resolve(Map.class));
            }
            if (resolvedType.getErasedType().equals(Optional.class)) {
                return getResolvedTypes(target, resolvedType.getTypeParameters().get(0));
            }
        }
        return List.of(target.getContext().resolve(resolvedType));
    }

    public void saveSchemaToFile(ObjectNode schema, String targetFolder, String fileName) {
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
