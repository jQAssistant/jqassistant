package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import com.google.common.base.CaseFormat;
import io.smallrye.config.WithDefault;

import static io.smallrye.config._private.ConfigLogging.log;

public class JsonSchemaGenerator {

    public ObjectNode generateSchema(Class<?> clazz, String path) throws IOException {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.JAVA_OBJECT).with(
                Option.NONSTATIC_NONVOID_NONGETTER_METHODS, Option.PLAIN_DEFINITION_KEYS, Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT,
                Option.FIELDS_DERIVED_FROM_ARGUMENTFREE_METHODS, Option.FLATTENED_ENUMS, Option.ALLOF_CLEANUP_AT_THE_END)
            .without(Option.VOID_METHODS, Option.GETTER_METHODS, Option.PUBLIC_STATIC_FIELDS);

        configBuilder.forMethods()
            .withTargetTypeOverridesResolver(target -> getResolvedTypes(target, target.getType()));
        configBuilder.forMethods()
            .withPropertyNameOverrideResolver(member -> mapToKebabCase(member.getName()));
        configBuilder.forTypesInGeneral()
            .withCustomDefinitionProvider(new MapDefinitionProvider());
        configBuilder.forMethods()
            .withDefaultResolver(method -> {
                WithDefault annotation = method.getAnnotationConsideringFieldAndGetter(WithDefault.class);
                if (annotation != null) {
                    return annotation.value();
                }
                return null;
            });
        configBuilder.forTypesInGeneral()
            .withDefinitionNamingStrategy((definitionKey, context) -> mapToKebabCase(definitionKey.getType()
                .getTypeName()));

        SchemaGenerator generator = new SchemaGenerator(configBuilder.build());
        ObjectNode schema = generator.generateSchema(clazz);
        ObjectNode finalSchema = wrapJqassistant(schema);
        saveSchemaToFile(finalSchema, path);
        return finalSchema;
    }

    /**
     * Splits the generated json schema into properties and $defs,
     * wraps the jqassistant root node around the properties part
     * and puts it back together with the $defs part.
     * Prohibits additionalProperties for the properties in jqassistant.
     *
     * @param schema
     *     the json schema generated with the victools schema generator
     * @return complete json schema for jqassistant.yaml
     */
    private static ObjectNode wrapJqassistant(ObjectNode schema) {
        ObjectNode propertiesNode = JsonNodeFactory.instance.objectNode();
        ObjectNode jqaWrapper = JsonNodeFactory.instance.objectNode();
        ObjectNode definitionWrapper = JsonNodeFactory.instance.objectNode();

        String properties = "properties";
        String object = "object";
        String defs = "$defs";
        String type = "type";

        for (Map.Entry<String, JsonNode> property : schema.properties()) {
            if (property.getKey()
                .equals(properties)) {
                propertiesNode.put(type, object);
                propertiesNode.set(properties, property.getValue());
                propertiesNode.put("additionalProperties", false);
            }
            if (property.getKey()
                .equals(defs)) {
                definitionWrapper.set(defs, property.getValue());
            }
        }
        jqaWrapper.set("jqassistant", propertiesNode);
        definitionWrapper.put(type, object);
        definitionWrapper.set(properties, jqaWrapper);
        return definitionWrapper;
    }

    private static void saveSchemaToFile(ObjectNode schema, String path) throws IOException {
        File file = new File(path);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValue(file, schema);
        log.info("Schema saved: " + file.getAbsolutePath());
    }

    private static List<ResolvedType> getResolvedTypes(MethodScope target, ResolvedType resolvedType) {
        if (resolvedType != null) {
            if (resolvedType.getErasedType()
                .equals(URI.class) || resolvedType.getErasedType()
                .equals(File.class)) {
                return List.of(target.getContext()
                    .resolve(String.class));
            }
            if (resolvedType.getErasedType()
                .equals(Optional.class)) {
                return getResolvedTypes(target, resolvedType.getTypeParameters()
                    .get(0));
            }
        }
        return List.of(target.getContext()
            .resolve(resolvedType));
    }

    private static String mapToKebabCase(String name) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, name);
    }

    public static class MapDefinitionProvider implements CustomDefinitionProviderV2 {

        @Override
        public CustomDefinition provideCustomSchemaDefinition(ResolvedType targetType, SchemaGenerationContext context) {
            if (!targetType.isInstanceOf(Map.class)) {
                return null;
            }
            ResolvedType keyType = context.getTypeContext()
                .getTypeParameterFor(targetType, Map.class, 0);
            ResolvedType valueType = context.getTypeContext()
                .getTypeParameterFor(targetType, Map.class, 1);
            if (keyType == null || !keyType.isInstanceOf(String.class)) {
                return null;
            }
            if (valueType == null) {
                valueType = context.getTypeContext()
                    .resolve(Object.class);
            }
            ObjectNode customSchema = context.getGeneratorConfig()
                .createObjectNode();
            customSchema.put(context.getKeyword(SchemaKeyword.TAG_TYPE), "object");
            ObjectNode unkownNameWrapper = context.getGeneratorConfig()
                .createObjectNode();
            ObjectNode valueTypeDefinition = context.createDefinition(valueType);
            unkownNameWrapper.set("^.*$", valueTypeDefinition);
            customSchema.set(context.getKeyword(SchemaKeyword.TAG_PATTERN_PROPERTIES), unkownNameWrapper);
            return new CustomDefinition(customSchema);
        }
    }

}
