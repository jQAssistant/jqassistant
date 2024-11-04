package com.buschmais.jqassistant.core.runtime.api.configuration;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class JsonSchemaGenerator {

    public ObjectNode generateSchema(Class clazz) {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        configBuilder.forTypesInGeneral()
            .withCustomDefinitionProvider((type, context) -> new CustomDefinition(handleStartingClass(context, type)));

        SchemaGenerator generator = new SchemaGenerator(configBuilder.build());
        ObjectNode jsonSchema = generator.generateSchema(clazz);
        saveSchemaToFile(jsonSchema, "target", "jsonSchema");
        return jsonSchema;
    }

    public ObjectNode handleStartingClass(SchemaGenerationContext context, ResolvedType type){
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        if (type.getErasedType().isInterface() && type.getErasedType().getAnnotation(ConfigMapping.class) != null) {
            schema.set(type.getErasedType().getAnnotation(ConfigMapping.class).prefix(), resolveFields(context, type));
        }
        return schema;
    }

    public ObjectNode resolveFields(SchemaGenerationContext context, ResolvedType type) {

        if (type.getErasedType().isInterface()) {
            ObjectNode schema = context.getGeneratorConfig().createObjectNode();

            schema.put("type", "object");
            TypeContext typeContext = context.getTypeContext();

            for (Method method : type.getErasedType().getMethods()) {
                ResolvedType returnType = typeContext.resolve(method.getGenericReturnType());


                String propertyName = method.getName();
                propertyName = mapCamelToKebabCase(propertyName);

                // for methods with annotations
                if (method.getAnnotation(WithName.class) != null) {
                    propertyName = mapCamelToKebabCase(method.getReturnType().getDeclaredAnnotation(WithName.class).value());
                }

                //for classes with annotations
                if (method.getReturnType().getDeclaredAnnotation(ConfigMapping.class) != null) {
                    String newName = method.getReturnType().getDeclaredAnnotation(ConfigMapping.class).prefix().replace(".", " ");
                    propertyName = newName.substring(newName.lastIndexOf(" ") + 1);
                }

                if (method.getAnnotation(WithDefault.class) != null) {
                    String defaultValue = method.getAnnotation(WithDefault.class).value();
                    schema.put("default", defaultValue);
                }


                JsonNode propertySchema = generateSchemaForSubInterfaces(returnType, context);
                schema.set(propertyName, propertySchema);
            }
            return schema;
        }
        return null;
    }

    private JsonNode generateSchemaForSubInterfaces(ResolvedType returnType, SchemaGenerationContext context) {
        Class<?> erasedType = returnType.getErasedType();
        if ((erasedType.isPrimitive() || erasedType == String.class || erasedType.getPackageName().startsWith("java."))) {

            ObjectNode schema = context.getGeneratorConfig().createObjectNode();
            schema.put("type", returnType.getErasedType().getSimpleName().toLowerCase());

            //handling arrays
            if ((returnType.isInstanceOf(List.class) || returnType.isInstanceOf(Collection.class) || returnType.isInstanceOf(Set.class)) && !returnType.getTypeParameters().isEmpty()) {
                schema.put("type", "array");
            }

            return schema;
        }
        return resolveFields(context, returnType);
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

    private static String mapCamelToKebabCase(String str) {
        return str.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
    }

}
