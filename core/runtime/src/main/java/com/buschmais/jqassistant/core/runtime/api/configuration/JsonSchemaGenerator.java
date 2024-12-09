package com.buschmais.jqassistant.core.runtime.api.configuration;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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

    public ObjectNode generateSchema(Class<?> clazz, String path) throws IOException {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.JAVA_OBJECT)
            .with(Option.MAP_VALUES_AS_ADDITIONAL_PROPERTIES, Option.NONSTATIC_NONVOID_NONGETTER_METHODS, Option.PLAIN_DEFINITION_KEYS,
                Option.FIELDS_DERIVED_FROM_ARGUMENTFREE_METHODS, Option.FLATTENED_ENUMS, Option.ALLOF_CLEANUP_AT_THE_END).without(Option.VOID_METHODS, Option.GETTER_METHODS);

        configBuilder.forMethods()
            .withTargetTypeOverridesResolver(target -> {
                ResolvedType resolvedType = target.getType();
                return getResolvedTypes(target, resolvedType);
            });
        configBuilder.forFields()
            .withIgnoreCheck(field -> field.getName().startsWith("PREFIX") || field.getName().startsWith("SKIP") || field.getName().startsWith("DEFAULT"));
        configBuilder.forMethods().withPropertyNameOverrideResolver((member) -> mapToKebabCase(member.getName()));
        configBuilder.forFields().withPropertyNameOverrideResolver((member) -> mapToKebabCase(member.getName()));

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
        ObjectNode schema = generator.generateSchema(clazz);
        ObjectNode finalSchema = wrapJqassistant(schema);
        saveSchemaToFile(finalSchema, path);
        return finalSchema;
    }

    private static ObjectNode wrapJqassistant(ObjectNode schema) {
        ObjectNode propertiesNode = JsonNodeFactory.instance.objectNode();
        ObjectNode jqaWrapper = JsonNodeFactory.instance.objectNode();
        ObjectNode definitionWrapper = JsonNodeFactory.instance.objectNode();
        for (Map.Entry<String, JsonNode> property : schema.properties()) {
            if (property.getKey().equals("properties")) {
                propertiesNode.put("type", "object");
                propertiesNode.set("properties", property.getValue());
                propertiesNode.put("additionalProperties", false);
            }
            if (property.getKey().equals("$defs")) {
                definitionWrapper.set("$defs", property.getValue());
            }
        }
        jqaWrapper.set("jqassistant", propertiesNode);
        definitionWrapper.put("type", "object");
        definitionWrapper.set("properties", jqaWrapper);
        return definitionWrapper;
    }

    private static void saveSchemaToFile(ObjectNode schema, String path) throws IOException {
        File file = new File(path);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            boolean directoriesCreated = parentDir.mkdirs();
            if (directoriesCreated) {
                System.out.println("Directory were created successfully");
            } else {
                System.out.println("Failed to create directory");
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, schema);
        System.out.println("Schema saved: " + file.getAbsolutePath());
    }

    private static List<ResolvedType> getResolvedTypes(MethodScope target, ResolvedType resolvedType) {
        if (resolvedType != null) {
            if (resolvedType.getErasedType().equals(URI.class) || resolvedType.getErasedType().equals(File.class)) {
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

    private static String mapToKebabCase(String name) {
        if (name.matches("^[A-Z0-9_]+$")) {
            return name.toLowerCase().replace("_", "-");
        }
        return name.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
    }

}
