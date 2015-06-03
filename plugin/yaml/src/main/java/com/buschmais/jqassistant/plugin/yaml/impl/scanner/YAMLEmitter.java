package com.buschmais.jqassistant.plugin.yaml.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLKeyBucket;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLKeyDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLValueBucket;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLValueDescriptor;
import org.apache.commons.lang.StringUtils;
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

import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.YAMLEmitter.EventType.DOCUMENT_END;
import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.YAMLEmitter.EventType.DOCUMENT_START;
import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.YAMLEmitter.EventType.MAPPING_START;
import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.YAMLEmitter.EventType.SCALAR;
import static com.buschmais.jqassistant.plugin.yaml.impl.scanner.YAMLEmitter.EventType.SEQUENCE_START;
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
                    YAMLDocumentDescriptor doc = currentScanner.getContext()
                                                               .getStore()
                                                               .create(YAMLDocumentDescriptor.class);
                    processingContext.pushContextEvent(typeOfEvent);
                    processingContext.push(doc);



                    break;

                case SEQUENCE_START:

                    if (processingContext.isContext(SEQUENCE_START)) {
                        // Sequence of sequences...
                        YAMLValueDescriptor valueDescriptor = currentScanner.getContext().getStore()
                                                                            .create(YAMLValueDescriptor.class);

                        processingContext.push(valueDescriptor);
                        processingContext.pushContextEvent(typeOfEvent);

                    } else {
                        processingContext.pushContextEvent(typeOfEvent);
                    }

                    break;

                case SEQUENCE_END:
                    if (processingContext.isContext(MAPPING_START, SCALAR, SEQUENCE_START)) {
                        processingContext.popContextEvent(2);
                        YAMLKeyDescriptor keyForSequence = processingContext.pop();
                        YAMLKeyBucket keyBucketForSequence = processingContext.peek();

                        keyBucketForSequence.getKeys().add(keyForSequence);
                    } else if (processingContext.isContext(DOCUMENT_START, SEQUENCE_START)) {
                        processingContext.popContextEvent(1);
                    } else if (processingContext.isContext(SEQUENCE_START, SEQUENCE_START)) {
                        processingContext.popContextEvent(1);
                        YAMLValueDescriptor vaDes1 = processingContext.pop();
                        YAMLValueBucket bbb = processingContext.peek();

                        bbb.getValues().add(vaDes1);
                        System.out.println(typeOfEvent);
                    } else {
                        throw new IllegalStateException("Not supported YAML structure.");
                    }

                    break;

                case DOCUMENT_END:
                    if (!processingContext.isContext(DOCUMENT_START)) {
                        throw new IllegalStateException("Not supported YAML structure.");
                    } else {

                        processingContext.popContextEvent(1);
                        YAMLDocumentDescriptor doc2 = processingContext.pop();
                        fileDescriptor.getDocuments().add(doc2);
                    }

                    break;

                case MAPPING_START:
                    processingContext.pushContextEvent(typeOfEvent);
                    break;

                case MAPPING_END:
                    if (processingContext.isContext(MAPPING_START)) {
                        processingContext.popContextEvent(1);
                    } else if (processingContext.isContext(MAPPING_START, SCALAR, MAPPING_START, SCALAR, SCALAR)) {
                        processingContext.popContextEvent(4);
                        YAMLKeyDescriptor currentKey = processingContext.pop();
                        YAMLKeyDescriptor parentKeyOfThis= processingContext.pop();
                        parentKeyOfThis.getKeys().add(currentKey);
                        YAMLKeyBucket parent = processingContext.peek();
                        parent.getKeys().add(parentKeyOfThis);
                    } else if (processingContext.isContext(MAPPING_START, SCALAR, SCALAR)) {
                        processingContext.popContextEvent(3);

                        YAMLKeyDescriptor keyDescriptor = processingContext.pop();

                        YAMLKeyBucket bucket2 = processingContext.peek();
                        bucket2.getKeys().add(keyDescriptor);

                    } else {
                        throw new IllegalStateException("Unsupported YAML structure.");
                    }

                    break;

                case SCALAR:
                    handleScalarEvent((ScalarEvent) event, typeOfEvent);
                    break;

                default:
                    // @todo
//                    System.out.println(typeOfEvent);

            }
        }


    }

    protected void handleScalarEvent(ScalarEvent event, EventType typeOfEvent) {
        if (processingContext.isContext(MAPPING_START)) {
            YAMLKeyDescriptor key = currentScanner.getContext().getStore()
                                                  .create(YAMLKeyDescriptor.class);

            String name = event.getValue();
            String fqn = processingContext. buildNextFQN(name);

            key.setName(trimToEmpty(name));
            key.setFullQualifiedName(trimToEmpty(fqn));

            processingContext.push(key);
            processingContext.pushContextEvent(typeOfEvent);

        } else if (processingContext.isContext(MAPPING_START, SCALAR)) {
            YAMLValueDescriptor value = currentScanner.getContext().getStore()
                                                      .create(YAMLValueDescriptor.class);

            String rawValue = event.getValue();

            value.setValue(rawValue);
            YAMLKeyDescriptor key = processingContext.peek();
            key.getValues().add(value);

            processingContext.pushContextEvent(typeOfEvent);

        } else if (processingContext.isContext(MAPPING_START, SCALAR, SCALAR)) {
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
            processingContext.pushContextEvent(typeOfEvent);

        } else if (processingContext.isContext(SEQUENCE_START)) {
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
            System.out.println(event);

            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.exit(1);
            throw new RuntimeException("Not implemented yet! " + event);
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


}
