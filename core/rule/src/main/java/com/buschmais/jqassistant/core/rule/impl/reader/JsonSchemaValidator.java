package com.buschmais.jqassistant.core.rule.impl.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import static java.lang.String.format;
import static java.util.Collections.emptySet;

class JsonSchemaValidator {
    private static final String JSON_SCHEMA_1_8 = "/META-INF/rule/jsonschema/jqassistant-rule-v1.8.schema.json";
    private final ObjectMapper mapper;
    private  JsonSchema schema;

    public JsonSchemaValidator() throws RuleException {
        mapper = new ObjectMapper(new YAMLFactory());
        JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory)
                                                           .objectMapper(mapper)
                                                           .build();

        try (InputStream inputStream = JsonSchemaValidator.class.getResourceAsStream(JSON_SCHEMA_1_8)) {
            schema = schemaFactory.getSchema(inputStream);
        } catch (IOException e) {
            String message = format("Failed to load schema from %s", JSON_SCHEMA_1_8);
            throw new RuleException(message);
        }
    }

    public ValidationResult validate(RuleSource ruleSource) throws IOException {
        ValidationResult result = new ValidationResult();

        try (InputStream inputStream = ruleSource.getInputStream()) {
            JsonNode rootNode = mapper.readTree(inputStream);

            if (rootNode.equals(MissingNode.getInstance())) {
                result.setSourceWasEmpty(true);
                result.setValidationMessages(emptySet());
            } else {
                Set<ValidationMessage> validationMessages = schema.validate(rootNode);

                result.setValidationMessages(validationMessages);
                result.setSourceWasEmpty(false);
            }
        }

        return result;
    }
}
