package com.buschmais.jqassistant.core.rule.impl.reader;

import java.util.*;

import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;

import static com.networknt.schema.ValidatorTypeCode.*;
import static java.lang.String.format;

public class ErrorMessageGenerator {
    private List<Generator> generators = Arrays.asList(new AdditionalProperty(),
                                                       new Type(),
                                                       new OneOf(),
                                                       new Enum(),
                                                       new RequiredProperty());

    public String generate(RuleSource ruleSource, Set<ValidationMessage> messages) {
        return generators.stream().map(g -> g.map(messages)).filter(Optional::isPresent)
                         .map(Optional::get)
                         .peek(c -> c.rule = ruleSource)
                         .findFirst()
                         .orElseThrow(() -> {
                             String message = "Failed to find error message generator for a set of validation messages";
                             return new IllegalStateException(message);
                         }).generate();
    }

    private static class Combination {
        RuleSource rule;
        ValidationMessage message;
        Generator generator;

        public Combination(Generator generator, ValidationMessage message) {
            this.generator = generator;
            this.message = message;
        }

        String generate() {
            return generator.generate(rule, message);
        }
    }

    static abstract class Generator {

        private final ValidatorTypeCode typeCode;

        public Generator(ValidatorTypeCode validatorTypeCode) {
            typeCode = validatorTypeCode;
        }

        Optional<Combination> map(Set<ValidationMessage> messages) {
            return messages.stream()
                           .filter(msg -> msg.getCode().equals(typeCode.getErrorCode()))
                           .findAny()
                           .map(msg -> new Combination(this, msg));
        }

        abstract String generate(RuleSource rule, ValidationMessage message);
    }

    static class OneOf extends Generator {
        public OneOf() {
            super(ONE_OF);
        }

        @Override
        String generate(RuleSource rule, ValidationMessage message) {
            String path = message.getPath();

            return format("Rule source '%s' can have only one of the given keywords at '%s'",
                          rule.getId(), path);
        }
    }

    static class Enum extends Generator {
        public Enum() {
            super(ENUM);
        }

        @Override
        String generate(RuleSource rule, ValidationMessage message) {
            String path = message.getPath();
            String validValues = message.getArguments()[0].replace("[", "").replace("]", "");

            return format("Rule source '%s' must have one of '%s' at '%s'",
                          rule.getId(),  validValues, path);
        }
    }

    static class Type extends Generator {
        private LinkedHashMap<String, String> typeNames = new LinkedHashMap<>();

        public Type() {
            super(TYPE);
            typeNames.put("array", "a sequence");
            typeNames.put("object", "a map");
            typeNames.put("string", "a scalar");
            typeNames.put("null", "nothing");
        }

        @Override
        String generate(RuleSource rule, ValidationMessage message) {
            String actualType = typeNames.get(message.getArguments()[0]);
            String expectedType = typeNames.get(message.getArguments()[1]);
            String source = rule.getId();
            String path = message.getPath();

            return format("Rule source '%s' contains at '%s' %s where %s " +
                          "is expected",
                          source, path, actualType, expectedType);
        }
    }

    static class RequiredProperty extends Generator {
        public RequiredProperty() {
            super(REQUIRED);
        }

        @Override
        String generate(RuleSource rule, ValidationMessage message) {
            String key = message.getArguments()[0];
            String path = message.getPath();
            String source = rule.getId();

            return format("Rule source '%s' misses the keyword '%s' at '%s'",
                          source, key, path);
        }
    }

    static class AdditionalProperty extends Generator {
        public AdditionalProperty() {
            super(ADDITIONAL_PROPERTIES);
        }

        @Override
        public String generate(RuleSource rule, ValidationMessage message) {
            String key = message.getArguments()[0];
            String path = message.getPath();
            String source = rule.getId();

            return format("Rule source '%s' contains the unknown keyword '%s' at '%s'",
                          source, key, path);
        }
    }
}
