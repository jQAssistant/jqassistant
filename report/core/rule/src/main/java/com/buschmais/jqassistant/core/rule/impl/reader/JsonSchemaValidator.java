package com.buschmais.jqassistant.core.rule.impl.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;

import static java.lang.String.format;

class JsonSchemaValidator {
    private static final String JSON_SCHEMA = "/META-INF/schema/jqassistant-rule-v2.9.schema.json";
    private final ObjectMapper mapper;
    private final Schema schema;

    public JsonSchemaValidator() throws RuleException {
        mapper = new ObjectMapper(new YAMLFactory());
        SchemaRegistry schemaFactory = SchemaRegistry.withDialect(Dialects.getDraft201909());

        try (InputStream inputStream = JsonSchemaValidator.class.getResourceAsStream(JSON_SCHEMA)) {
            schema = schemaFactory.getSchema(inputStream);
        } catch (IOException e) {
            throw new RuleException(format("Failed to load schema from %s", JSON_SCHEMA));
        }
    }

    public ValidationResult validate(RuleSource ruleSource) throws IOException {
        try (InputStream inputStream = ruleSource.getURL()
            .openStream()) {
            JsonNode rootNode = mapper.readTree(inputStream);
            List<Error> errors = schema.validate(rootNode);
            return ValidationResult.builder()
                .validationMessages(errors)
                .sourceWasEmpty(false)
                .build();
        }
    }
}
