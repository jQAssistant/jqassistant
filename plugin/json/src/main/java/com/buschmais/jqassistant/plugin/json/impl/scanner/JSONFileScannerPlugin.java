package com.buschmais.jqassistant.plugin.json.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FilePatternMatcher;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.json.api.model.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS;

@Slf4j
@ScannerPlugin.Requires(FileDescriptor.class)
public class JSONFileScannerPlugin extends AbstractScannerPlugin<FileResource, JSONFileDescriptor> {

    public static final String PROPERTY_INCLUDE = "json.file.include";
    public static final String PROPERTY_EXCLUDE = "json.file.exclude";

    private ObjectMapper objectMapper;

    private FilePatternMatcher filePatternMatcher;

    protected FilePatternMatcher getFilePatternMatcher() {
        return filePatternMatcher;
    }

    @Override
    public void initialize() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(ALLOW_COMMENTS);
        this.objectMapper.enable(FAIL_ON_TRAILING_TOKENS);
    }

    @Override
    protected void configure() {
        String inclusionPattern = getStringProperty(PROPERTY_INCLUDE, "*.json");
        String exclusionPattern = getStringProperty(PROPERTY_EXCLUDE, null);
        filePatternMatcher = FilePatternMatcher.builder()
            .include(inclusionPattern)
            .exclude(exclusionPattern)
            .build();
    }

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) {
        return filePatternMatcher.accepts(path);

    }

    @Override
    public JSONFileDescriptor scan(final FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        Store store = context.getStore();
        FileDescriptor fileDescriptor = context.getCurrentDescriptor();
        JSONFileDescriptor jsonFileDescriptor = store.addDescriptorType(fileDescriptor, JSONFileDescriptor.class);

        try (InputStream inputStream = item.createStream()) {
            // use UTF-8 for decoding and be tolerant against invalid encodings
            CharsetDecoder charsetDecoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.IGNORE);
            InputStreamReader reader = new InputStreamReader(inputStream, charsetDecoder);

            JsonNode jsonNode = objectMapper.readTree(reader);
            JSONValueDescriptor valueDescriptor = toDescriptor(jsonNode, store);
            if (valueDescriptor != null) {
                jsonFileDescriptor.setValue(valueDescriptor);
                jsonFileDescriptor.setValid(true);
            } else {
                jsonFileDescriptor.setValid(false);
            }
        } catch (JsonProcessingException e) {
            jsonFileDescriptor.setValid(false);
        }
        return jsonFileDescriptor;
    }

    private JSONValueDescriptor toDescriptor(JsonNode jsonNode, Store store) {
        switch (jsonNode.getNodeType()) {
        case BOOLEAN:
            return toScalarValue(jsonNode.booleanValue(), store);
        case STRING:
            return toScalarValue(jsonNode.textValue(), store);
        case NUMBER:
            return numberToScalarValue(jsonNode.numberValue(), store);
        case NULL:
            return numberToScalarValue(null, store);
        case OBJECT:
            JSONObjectDescriptor objectDescriptor = store.create(JSONObjectDescriptor.class);
            for (Map.Entry<String, JsonNode> property : jsonNode.properties()) {
                JSONKeyDescriptor keyDescriptor = store.create(JSONKeyDescriptor.class);
                keyDescriptor.setName(property.getKey());
                JSONValueDescriptor valueDescriptor = toDescriptor(property.getValue(), store);
                keyDescriptor.setValue(valueDescriptor);
                objectDescriptor.getKeys()
                    .add(keyDescriptor);
            }
            return objectDescriptor;
        case ARRAY:
            JSONArrayDescriptor arrayDescriptor = store.create(JSONArrayDescriptor.class);
            Iterator<JsonNode> elements = jsonNode.elements();
            while (elements.hasNext()) {
                JsonNode element = elements.next();
                JSONValueDescriptor valueDescriptor = toDescriptor(element, store);
                if (valueDescriptor != null) {
                    arrayDescriptor.getValues()
                        .add(valueDescriptor);
                }
            }
            return arrayDescriptor;
        case MISSING:
            return null;
        default:
            log.info("Encountered unknown JSON node: {}", jsonNode);
            return null;
        }
    }

    private static JSONScalarValueDescriptor numberToScalarValue(Number value, Store store) {
        if (value instanceof BigDecimal || value instanceof BigInteger) {
            return toScalarValue(value.toString(), store);
        }
        return toScalarValue(value, store);
    }

    private static JSONScalarValueDescriptor toScalarValue(Object value, Store store) {
        JSONScalarValueDescriptor valueDescriptor = store.create(JSONScalarValueDescriptor.class);
        valueDescriptor.setValue(value);
        return valueDescriptor;
    }
}
