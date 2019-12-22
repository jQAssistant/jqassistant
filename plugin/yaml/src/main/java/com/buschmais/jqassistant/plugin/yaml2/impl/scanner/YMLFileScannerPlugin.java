package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.yaml2.api.model.*;

import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Parse;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.ScalarEvent;

import static java.lang.String.format;
import static org.snakeyaml.engine.v2.events.Event.ID.Scalar;

@ScannerPlugin.Requires(FileDescriptor.class)
public class YMLFileScannerPlugin extends AbstractScannerPlugin<FileResource, YMLFileDescriptor> {

    /**
     * Supported file extensions for YAML file resources.
     */
    public final static String YAML_FILE_EXTENSION = ".yaml";
    public final static String YML_FILE_EXTENSION = ".yml";

    private ParsingContext context = new ParsingContext();

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) {
        String lowercasePath = path.toLowerCase();
        return lowercasePath.endsWith(YAML_FILE_EXTENSION) || lowercasePath.endsWith(YML_FILE_EXTENSION);
    }

    @Override
    public YMLFileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        LoadSettings settings = LoadSettings.builder().build();
        FileDescriptor fileDescriptor = context.getCurrentDescriptor();

        // todo implement handleFileEnd
        // todo take it from the parsing context
        YMLFileDescriptor yamlFileDescriptor = handleFileStart(fileDescriptor);


        try (InputStream in = item.createStream()) {
            Parse parser = new Parse(settings);
            Iterable<Event> events = parser.parseInputStream(in);
            processEvents(events);
        } catch (RuntimeException re) {
            // todo Improve the errorhandling
            throw re;
        }

        return yamlFileDescriptor;
    }

    private void processEvents(Iterable<Event> events) {
        // verbrauchend
        // events.forEach(e -> { System.out.print(e.getClass() + "##");System.out.println("##" + e + "##"); });

        for (Event event : events) {
            System.out.println("## " + event);
            switch (event.getEventId()) {
                case StreamStart:
                    handleStreamStart(event);
                    break;
                case DocumentStart:
                    handleDocumentStart(event);
                    break;
                case SequenceStart:
                    handleSequenceStart(event);
                    break;
                case Scalar:
                    boolean scalarIsValueForKey = context.isInKey();
                    handleScalar(event);
                    if (scalarIsValueForKey) {
                        leaveCurrentContext(event);
                    }
                    break;
                case MappingStart:
                    boolean mapIsValueForKey = context.isInKey();
                    handleMapStart(event);
                    if (mapIsValueForKey) {
                        context.getCurrent().setKeyForValue(mapIsValueForKey);
                    }
                    break;
                case MappingEnd:
                case SequenceEnd:
                case DocumentEnd:
                case StreamEnd:
                    boolean wasValueForKey = context.getCurrent().isKeyForValue();
                    leaveCurrentContext(event);

                    if (wasValueForKey) {
                        leaveCurrentContext(event);
                    }

                    break;
                    // todo no default ;-(
            }
        }


    }

    private void handleMapStart(Event event) {
        YMLMapDescriptor mapDescriptor = createDescriptor(YMLMapDescriptor.class);
        ContextType<YMLDescriptor> contextType = context.getCurrent();

        ContextType<YMLMapDescriptor> inMap = ContextType.ofInMap(mapDescriptor);


        if (context.isInDocument()) {
            YMLDocumentDescriptor documentDescriptor = (YMLDocumentDescriptor) contextType.getDescriptor();
            documentDescriptor.getMaps().add(mapDescriptor);

        } else if (context.isInSequence()) {
            int index = contextType.getPositionalContext().inc();
            YMLSequenceDescriptor sequenceDescriptor = (YMLSequenceDescriptor) contextType.getDescriptor();
            mapDescriptor.setIndex(index);
            sequenceDescriptor.getMaps().add(mapDescriptor);
        } else if (context.isInKey()) {
            getScannerContext().getStore().addDescriptorType(mapDescriptor, YMLValueDescriptor.class);
            YMLKeyDescriptor ymlKeyDescriptor = (YMLKeyDescriptor) contextType.getDescriptor();
            ymlKeyDescriptor.setValue(mapDescriptor);
            // todo
        } else {
            throw new IllegalStateException();
        }

        context.enter(inMap);
    }

    private void handleScalar(Event event) {
        ContextType<YMLDescriptor> contextType = this.context.getCurrent();

        // todo Add support for tags
        // todo Add support for impl

        if (context.isInSequence()) {
            int index = contextType.getPositionalContext().inc();
            YMLSequenceDescriptor sequenceDescriptor = (YMLSequenceDescriptor) contextType.getDescriptor();
            YMLScalarDescriptor scalarDescriptor = createDescriptor(YMLScalarDescriptor.class);

            scalarDescriptor.setIndex(index);
            scalarDescriptor.setValue(((ScalarEvent) event).getValue());
            sequenceDescriptor.getScalars().add(scalarDescriptor);
        } else if (context.isInMap() && event.isEvent(Scalar)) {
            YMLMapDescriptor mapDescriptor = (YMLMapDescriptor) contextType.getDescriptor();
            YMLKeyDescriptor keyDescriptor = createDescriptor(YMLKeyDescriptor.class);
            ContextType<YMLKeyDescriptor> newContextType = ContextType.ofInKey(keyDescriptor);
            context.enter(newContextType);

            keyDescriptor.setName(((ScalarEvent) event).getValue());
            mapDescriptor.getKeys().add(keyDescriptor);
        } else if (context.isInKey() && event.isEvent(Scalar)) {
            YMLScalarDescriptor valueDescriptor = createDescriptor(YMLScalarDescriptor.class);
            YMLKeyDescriptor keyDescriptor = (YMLKeyDescriptor) contextType.getDescriptor();
            addDescriptor(valueDescriptor, YMLValueDescriptor.class);
            valueDescriptor.setValue(((ScalarEvent) event).getValue());
            keyDescriptor.setValue(valueDescriptor);
            // context.leave();
        } else {
            String fqcn = contextType.getClass().getCanonicalName();
            String message = format("Unsupported YAML element represented by " +
                                    "class %s encountered.", fqcn);
            // todo throw new IllegalStateException(message  );
        }

        // todo Handle unsupported descriptor
    }


    private YMLFileDescriptor handleFileStart(FileDescriptor fileDescriptor) {
        YMLFileDescriptor yamlFileDescriptor = getScannerContext().getStore().addDescriptorType(fileDescriptor, YMLFileDescriptor.class);
        ContextType<YMLFileDescriptor> inFile = ContextType.ofInFile(yamlFileDescriptor);
        context.enter(inFile);
        return yamlFileDescriptor;
    }

    private void leaveCurrentContext(Event event) {
        context.leave();
    }

    private void handleSequenceStart(Event event) {
        // todo can we assert here something useful?

        YMLSequenceDescriptor ymlSequenceDescriptor = createDescriptor(YMLSequenceDescriptor.class);
        ContextType<YMLSequenceDescriptor> inSequence = ContextType.ofInSequence(ymlSequenceDescriptor);

        if (context.isInDocument()) {
            ((YMLDocumentDescriptor)context.getCurrent().getDescriptor()).getSequences().add(ymlSequenceDescriptor);
        } else if (context.isInSequence()) {
            int index = context.getCurrent().getPositionalContext().inc();
            ymlSequenceDescriptor.setIndex(index);
            YMLSequenceDescriptor descriptor = (YMLSequenceDescriptor) context.getCurrent().getDescriptor();
            descriptor.getSequences().add(ymlSequenceDescriptor);
        } else {
            // todo check if the type of the exeption is correct or if there is a better one
            String fqcn = context.getCurrent().getDescriptor().getClass().getCanonicalName();
            String message = format("Unsupported YAML element represented by " +
                                    "class %s encountered.", fqcn);
            throw new IllegalStateException(message  );
        }

        context.enter(inSequence);
    }

    private void handleDocumentStart(Event event) {
        if (context.isNotInStream()) {
            throwIllegalStateException(ContextType.Type.IN_SEQUENCE, context.peek().getType());
        }

        YMLDocumentDescriptor descriptor = createDescriptor(YMLDocumentDescriptor.class);
        ContextType<YMLDocumentDescriptor> inDocument = ContextType.ofInDocument(descriptor);

        context.enter(inDocument);

        ContextType<YMLFileDescriptor> fileContext = context.getAncestor(ContextType.Ancestor.SECOND);
        YMLFileDescriptor ymlFileDescriptor = fileContext.getDescriptor();
        ymlFileDescriptor.getDocuments().add(inDocument.getDescriptor());
    }

    private <T extends Descriptor, D extends YMLDescriptor> void addDescriptor(T descriptor, Class<D> descriptorType) {
        getScannerContext().getStore().addDescriptorType(descriptor, descriptorType);
    }

    private <D extends YMLDescriptor> D createDescriptor(Class<D> descriptorType) {
        return getScannerContext().getStore().create(descriptorType);
    }

    private void handleStreamStart(Event event) {
        ContextType<?> inStream = ContextType.ofInStream();
        context.enter(inStream);
    }

    private void throwIllegalStateException(ContextType.Type expected,
                                            ContextType.Type actual) {
        // todo Which type of exception to throw in case of a wrong state
        String message = format("Wrong internal state during parsing a YAML " +
                                "document. Expected content: %d, actual " +
                                "context: %d", expected, actual);

        throw new IllegalStateException(message);
    }
}
