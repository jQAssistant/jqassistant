package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import com.google.common.base.CaseFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.smallrye.config.WithDefault;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import static com.buschmais.jqassistant.core.runtime.api.bootstrap.VersionProvider.getVersionProvider;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
@Slf4j
public class JsonSchemaGenerator {

    public static ObjectNode generateSchema(Class<?> clazz) {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.JAVA_OBJECT).with(
                Option.NONSTATIC_NONVOID_NONGETTER_METHODS, Option.PLAIN_DEFINITION_KEYS, Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT,
                Option.FIELDS_DERIVED_FROM_ARGUMENTFREE_METHODS, Option.FLATTENED_ENUMS, Option.ALLOF_CLEANUP_AT_THE_END)
            .without(Option.VOID_METHODS, Option.GETTER_METHODS, Option.PUBLIC_STATIC_FIELDS);

        configBuilder.forMethods()
            .withTargetTypeOverridesResolver(target -> getResolvedTypes(target, target.getType()))
            .withPropertyNameOverrideResolver(member -> mapToKebabCase(member.getName()))
            .withDefaultResolver(method -> {
                WithDefault annotation = method.getAnnotationConsideringFieldAndGetter(WithDefault.class);
                if (annotation != null) {
                    return annotation.value();
                }
                return null;
            })
            .withDescriptionResolver(method -> {
                Description annotation = method.getAnnotationConsideringFieldAndGetter(Description.class);
                if (annotation != null) {
                    return annotation.value();
                }
                if (method.getType() != null) {
                    Description innerAnnotation = method.getType()
                        .getErasedType()
                        .getAnnotation(Description.class);
                    if (innerAnnotation != null) {
                        return innerAnnotation.value();
                    }
                }
                return null;
            });

        configBuilder.forTypesInGeneral()
            .withCustomDefinitionProvider(new MapDefinitionProvider())
            .withDefinitionNamingStrategy((definitionKey, context) -> mapToKebabCase(definitionKey.getType()
                .getTypeName()));

        SchemaGenerator generatorNew = new SchemaGenerator(configBuilder.build());
        SchemaBuilder schemaBuilder = generatorNew.buildMultipleSchemaDefinitions();
        schemaBuilder.createSchemaReference(clazz); // has to be called twice to initiate definition generating
        ObjectNode schema = schemaBuilder.createSchemaReference(clazz);

        ObjectNode definitions = schemaBuilder.collectDefinitions("$defs");
        return wrapJqassistant(schema, definitions);
    }

    /**
     * Takes the schema properties and definition nodes,
     * wraps the jqassistant root node around the property nodes
     * (one for standard and one for profiles) and combines them.
     * Indirectly allows additional properties at highest level by not prohibiting them.
     *
     * @param properties
     *     the json representation of the jqassistant properties generated with the victools schema generator
     * @param definitions
     *     generated definitions or the schema properties
     * @return complete json schema for jqassistant.yaml
     */
    private static ObjectNode wrapJqassistant(ObjectNode properties, ObjectNode definitions) {
        ObjectNode jqaWrapper = JsonNodeFactory.instance.objectNode();
        ObjectNode definitionWrapper = JsonNodeFactory.instance.objectNode();
        ObjectNode profileWrapper = JsonNodeFactory.instance.objectNode();
        ObjectNode patternPropertiesWrapper = JsonNodeFactory.instance.objectNode();

        jqaWrapper.set("jqassistant", properties);
        profileWrapper.put("type", "object");
        profileWrapper.set("properties", jqaWrapper);
        patternPropertiesWrapper.set("^%.*$", profileWrapper);
        definitionWrapper.set("$defs", definitions);
        definitionWrapper.put("type", "object");
        definitionWrapper.set("properties", jqaWrapper);
        definitionWrapper.set("patternProperties", patternPropertiesWrapper);
        return definitionWrapper;
    }

    /**
     * Handles type resolution of URI, File and Optional properties.
     */
    private static List<ResolvedType> getResolvedTypes(MethodScope target, ResolvedType resolvedType) {
        if (resolvedType != null) {
            if (resolvedType.getErasedType()
                .equals(URI.class) || resolvedType.getErasedType()
                .equals(File.class) || resolvedType.getErasedType()
                .equals(ZonedDateTime.class)) {
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

    /**
     * Handles the schema definitions for maps with different value types (targetType).
     * Assumes that maps are always of the form &lt;String, targetType&gt; and cases deviating
     * from this structure are not handled.
     *
     * <p>The schema definition for the map properties is manually set as a type "object".
     * A schema definition for the value type (e.g., Boolean, Java classes, etc.) is generated
     * dynamically. Since the property name (key) in the map is custom, a placeholder
     * node with a regex-based name pattern ("^.*$") is used, and the value type definition
     * is assigned to this node.</p>
     * <p>
     * Based on the idea of https://github.com/victools/jsonschema-generator/blob/main/jsonschema-examples/src/main/java/com/github/victools/jsonschema/examples/EnumMapExample.java
     */
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
            ObjectNode unknownNameWrapper = context.getGeneratorConfig()
                .createObjectNode();
            ObjectNode valueTypeDefinition = context.createDefinition(valueType);
            unknownNameWrapper.set("^.*$", valueTypeDefinition);
            customSchema.set(context.getKeyword(SchemaKeyword.TAG_PATTERN_PROPERTIES), unknownNameWrapper);
            return new CustomDefinition(customSchema);
        }
    }

    public static File writeSchema(ObjectNode schema, File directory, String fileNamePrefix) throws IOException {
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = fileNamePrefix + "-v" + getVersionProvider().getMajorVersion() + "." + getVersionProvider().getMinorVersion() + ".schema.json";

        File file = new File(directory, fileName);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValue(file, schema);
        log.info("Schema saved: " + file.getAbsolutePath());
        return file;
    }

    /**
     * Helper method for validating a jqassistant.yaml example file to ensure the right behaviour of the schema generator.
     */
    public static Set<ValidationMessage> validateYaml(URL configResource, JsonNode schemaNode) throws Exception {
        JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory)
            .build();
        JsonSchema schema = schemaFactory.getSchema(schemaNode);
        ObjectMapper objectMapper = new ObjectMapper();
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData;
        try (InputStream inputStream = configResource.openStream()) {
            yamlData = yaml.load(inputStream);
        }
        String jsonString = objectMapper.writeValueAsString(yamlData);
        Set<ValidationMessage> validationMessages = schema.validate(objectMapper.readTree(jsonString));
        if (!validationMessages.isEmpty()) {
            log.error(validationMessages.toString());
        }
        return validationMessages;
    }
}
