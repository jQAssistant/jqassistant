package com.buschmais.jqassistant.plugin.yaml.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLKeyBucket;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLKeyDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLValueBucket;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLValueDescriptor;
import org.yaml.snakeyaml.emitter.Emitable;
import org.yaml.snakeyaml.events.DocumentEndEvent;
import org.yaml.snakeyaml.events.DocumentStartEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingEndEvent;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceEndEvent;
import org.yaml.snakeyaml.events.SequenceStartEvent;
import org.yaml.snakeyaml.events.StreamEndEvent;
import org.yaml.snakeyaml.events.StreamStartEvent;

import java.io.IOException;

import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.YAMLEmitter.ParseContext.DOCUMENT_CTX;
import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.YAMLEmitter.ParseContext.MAPPING_CXT;
import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.YAMLEmitter.ParseContext.MAPPING_KEY_CXT;
import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.YAMLEmitter.EventType.MAPPING_START;
import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.YAMLEmitter.ParseContext.MAPPING_VALUE_CXT;
import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.YAMLEmitter.ParseContext.SEQUENCE_CXT;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

/**
 * Emitter used to build the graph for a found YAML file.
 */
class YAMLEmitter implements Emitable {
    private final YAMLFileDescriptor fileDescriptor;
    private final Scanner currentScanner;
    private ProcessingContext processingContext = new ProcessingContext();

    public YAMLEmitter(YAMLFileDescriptor yamlFileDescriptor, Scanner scanner) {
        fileDescriptor = yamlFileDescriptor;
        currentScanner = scanner;

        processingContext.push(yamlFileDescriptor);
    }

    @Override
    public void emit(Event event) throws IOException {
        EventType typeOfEvent = toEventType(event);

        if (typeOfEvent != null) {
            switch (typeOfEvent) {
                case DOCUMENT_START:
                    handleDocumentStartEvent();
                    break;

                case SEQUENCE_START:
                    handleSequenceStart();
                    break;

                case SEQUENCE_END:
                    handleSequenceEnd();
                    break;

                case DOCUMENT_END:
                    handleDocumentEndEvent();
                    break;

                case MAPPING_START:
                    handleMappingStartEvent();
                    break;

                case MAPPING_END:
                    handleMappingEndEvent();
                    break;

                case SCALAR:
                    handleScalarEvent((ScalarEvent) event);
                    break;

                default:
                    unsupportedYAMLStructure();
            }
        }
    }

    protected void handleSequenceStart() {
        if (processingContext.isContext(SEQUENCE_CXT)) {
            // Sequence of sequences...
            YAMLValueDescriptor valueDescriptor = currentScanner.getContext().getStore()
                                                                .create(YAMLValueDescriptor.class);

            processingContext.push(valueDescriptor);
            processingContext.pushContextEvent(SEQUENCE_CXT);

        } else {
            processingContext.pushContextEvent(SEQUENCE_CXT);
        }
    }

    protected void handleSequenceEnd() {
        if (processingContext.isContext(MAPPING_CXT, MAPPING_KEY_CXT, SEQUENCE_CXT)) {
            processingContext.popContextEvent(2);
            YAMLKeyDescriptor keyForSequence = processingContext.pop();
            YAMLKeyBucket keyBucketForSequence = processingContext.peek();

            keyBucketForSequence.getKeys().add(keyForSequence);
        } else if (processingContext.isContext(DOCUMENT_CTX, SEQUENCE_CXT)) {
            processingContext.popContextEvent(1);
        } else if (processingContext.isContext(SEQUENCE_CXT, SEQUENCE_CXT)) {
            processingContext.popContextEvent(1);
            YAMLValueDescriptor value = processingContext.pop();
            YAMLValueBucket bbb = processingContext.peek();

            bbb.getValues().add(value);
        } else {
            unsupportedYAMLStructure();
        }
    }

    protected void handleDocumentEndEvent() {
        if (!processingContext.isContext(DOCUMENT_CTX)) {
            unsupportedYAMLStructure();
        } else {

            processingContext.popContextEvent(1);
            YAMLDocumentDescriptor doc = processingContext.pop();
            fileDescriptor.getDocuments().add(doc);
        }
    }

    protected void handleDocumentStartEvent() {
        YAMLDocumentDescriptor doc = currentScanner.getContext()
                                                   .getStore()
                                                   .create(YAMLDocumentDescriptor.class);
        processingContext.pushContextEvent(DOCUMENT_CTX);
        processingContext.push(doc);
    }

    protected void handleMappingStartEvent() {
        processingContext.pushContextEvent(MAPPING_CXT);
    }

    protected void handleMappingEndEvent() {
        if (processingContext.isContext(MAPPING_CXT, MAPPING_KEY_CXT, MAPPING_CXT)) {
            processingContext.popContextEvent(2);

            YAMLKeyDescriptor currentKey = processingContext.pop();
            YAMLKeyBucket parent = processingContext.peek();

            parent.getKeys().add(currentKey);
        } else if (processingContext.isContext(MAPPING_CXT)) {
            processingContext.popContextEvent(1);

        } else if (processingContext.isContext(MAPPING_CXT, MAPPING_KEY_CXT, MAPPING_CXT, MAPPING_KEY_CXT, MAPPING_VALUE_CXT)) {
            processingContext.popContextEvent(4);
            YAMLKeyDescriptor currentKey = processingContext.pop();
            YAMLKeyDescriptor parentKeyOfThis= processingContext.pop();
            YAMLKeyBucket parent = processingContext.peek();

            parentKeyOfThis.getKeys().add(currentKey);
            parent.getKeys().add(parentKeyOfThis);

        } else if (processingContext.isContext(MAPPING_CXT, MAPPING_KEY_CXT, MAPPING_VALUE_CXT)) {
            processingContext.popContextEvent(3);

            YAMLKeyDescriptor keyDescriptor = processingContext.pop();

            YAMLKeyBucket bucket = processingContext.peek();
            bucket.getKeys().add(keyDescriptor);

        } else {
            unsupportedYAMLStructure();
        }
    }

    protected void handleScalarEvent(ScalarEvent event) {
        if (processingContext.isContext(MAPPING_CXT)) {
            YAMLKeyDescriptor key = currentScanner.getContext().getStore()
                                                  .create(YAMLKeyDescriptor.class);

            String name = event.getValue();
            String fqn = processingContext.buildNextFQN(name);

            key.setName(trimToEmpty(name));
            key.setFullQualifiedName(trimToEmpty(fqn));

            processingContext.push(key);
            processingContext.pushContextEvent(MAPPING_KEY_CXT);

        } else if (processingContext.isContext(MAPPING_CXT, MAPPING_KEY_CXT)) {
            YAMLValueDescriptor value = currentScanner.getContext().getStore()
                                                      .create(YAMLValueDescriptor.class);

            String rawValue = event.getValue();

            value.setValue(rawValue);
            YAMLKeyDescriptor key = processingContext.peek();
            key.getValues().add(value);

            processingContext.pushContextEvent(MAPPING_VALUE_CXT);

        } else if (processingContext.isContext(MAPPING_CXT, MAPPING_KEY_CXT, MAPPING_VALUE_CXT)) {
            processingContext.popContextEvent(2);
            YAMLKeyDescriptor key = processingContext.pop();

            YAMLKeyBucket bucket = processingContext.peek();
            bucket.getKeys().add(key);

            YAMLKeyDescriptor nextKey = currentScanner.getContext().getStore()
                                                      .create(YAMLKeyDescriptor.class);

            String name = event.getValue();
            String fqn = processingContext.buildNextFQN(name);

            nextKey.setName(trimToEmpty(name));
            nextKey.setFullQualifiedName(trimToEmpty(fqn));

            processingContext.push(nextKey);
            processingContext.pushContextEvent(MAPPING_KEY_CXT);

        } else if (processingContext.isContext(SEQUENCE_CXT)) {
            YAMLValueDescriptor value = currentScanner.getContext()
                                                      .getStore()
                                                      .create(YAMLValueDescriptor.class);

            String rawValue = event.getValue();

            value.setValue(trimToEmpty(rawValue));

            YAMLValueBucket bucket = processingContext.peek();
            bucket.getValues().add(value);
        }
    }

    private EventType toEventType(Event event) {
        EventType result = null;

        if (event instanceof StreamEndEvent) {
            // Ignored
        } else if(event instanceof StreamStartEvent) {
            // Ignored
        } else if (event instanceof MappingStartEvent) {
            result = MAPPING_START;
        } else if (event instanceof MappingEndEvent) {
            result = EventType.MAPPING_END;
        } else if (event instanceof DocumentStartEvent) {
            result = EventType.DOCUMENT_START;
        } else if (event instanceof DocumentEndEvent) {
            result = EventType.DOCUMENT_END;
        } else if (event instanceof ScalarEvent) {
            result = EventType.SCALAR;
        } else if (event instanceof SequenceStartEvent) {
            result = EventType.SEQUENCE_START;
        } else if (event instanceof SequenceEndEvent) {
            result = EventType.SEQUENCE_END;
        } else {
            unsupportedYAMLStructure();
        }

        return result;
    }

    public enum EventType {
        DOCUMENT_END,
        DOCUMENT_START,
        MAPPING_END,
        MAPPING_START,
        SCALAR,
        SEQUENCE_END,
        SEQUENCE_START
    }

    public enum ParseContext {
        DOCUMENT_CTX,
        MAPPING_CXT,
        MAPPING_KEY_CXT,
        MAPPING_VALUE_CXT,
        SEQUENCE_CXT,
    }

    private static void unsupportedYAMLStructure() {
        throw new RuntimeException("Not supported YAML structure. Please " +
                                        "report this with an example YAML document.");
    }

}
