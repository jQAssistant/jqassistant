package com.buschmais.jqassistant.plugin.yaml.impl.scanner;

import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLKeyDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

public class ProcessingContextTest {

    private ProcessingContext context;

    @Before
    public void setUp() {
        context = new ProcessingContext();
    }

    @Test
    public void isContextReturnsFalseIfEventContextStackIsEmpty() {
        assertThat(context.isContext(YAMLEmitter.ParseContext.MAPPING_CXT),
                   equalTo(false));
    }

    @Test
    public void buildNextFQNWorksForLongName() {
        YAMLDocumentDescriptor docDescriptor = Mockito.mock(YAMLDocumentDescriptor.class);

        YAMLKeyDescriptor keyADescriptor = Mockito.mock(YAMLKeyDescriptor.class);

        doReturn("A").when(keyADescriptor).getName();
        doReturn(singletonList(keyADescriptor)).when(docDescriptor).getKeys();

        YAMLKeyDescriptor keyBDescriptor = Mockito.mock(YAMLKeyDescriptor.class);

        doReturn("B").when(keyBDescriptor).getName();
        doReturn(singletonList(keyBDescriptor)).when(keyADescriptor).getKeys();

        YAMLKeyDescriptor keyCDescriptor = Mockito.mock(YAMLKeyDescriptor.class);

        doReturn("C").when(keyCDescriptor).getName();
        doReturn(singletonList(keyCDescriptor)).when(keyBDescriptor).getKeys();

        YAMLKeyDescriptor keyDDescriptor = Mockito.mock(YAMLKeyDescriptor.class);

        doReturn("D").when(keyDDescriptor).getName();
        doReturn(singletonList(keyDDescriptor)).when(keyCDescriptor).getKeys();

        context.push(docDescriptor);
        context.push(keyADescriptor);
        context.push(keyBDescriptor);
        context.push(keyCDescriptor);
        context.push(keyDDescriptor);

        assertThat(context.buildNextFQN("EON"), equalTo("A.B.C.D.EON"));
    }

    @Test
    public void isContextReturnsFalseIfRequestedContextPathIfLongerThenActualStack() {
        context.pushContextEvent(YAMLEmitter.ParseContext.DOCUMENT_CTX);
        context.pushContextEvent(YAMLEmitter.ParseContext.MAPPING_CXT);

        assertThat(context.isContext(YAMLEmitter.ParseContext.DOCUMENT_CTX, YAMLEmitter.ParseContext.MAPPING_CXT,
                                     YAMLEmitter.ParseContext.SEQUENCE_CXT),
                   equalTo(false));
    }

    @Test
    public void isContextReturnsTrueIfRequestedPathAndActualStackAreIdentically() {
        context.pushContextEvent(YAMLEmitter.ParseContext.DOCUMENT_CTX);
        context.pushContextEvent(YAMLEmitter.ParseContext.MAPPING_CXT);
        context.pushContextEvent(YAMLEmitter.ParseContext.SEQUENCE_CXT);

        assertThat(context.isContext(YAMLEmitter.ParseContext.DOCUMENT_CTX, YAMLEmitter.ParseContext.MAPPING_CXT,
                                     YAMLEmitter.ParseContext.SEQUENCE_CXT),
                   equalTo(true));
    }

    @Test
    public void isContextReturnsFalseIfRequestedPathAndActualStackDifferInOneElement() {
        context.pushContextEvent(YAMLEmitter.ParseContext.DOCUMENT_CTX);
        context.pushContextEvent(YAMLEmitter.ParseContext.MAPPING_CXT);
        context.pushContextEvent(YAMLEmitter.ParseContext.SEQUENCE_CXT);

        assertThat(context.isContext(YAMLEmitter.ParseContext.DOCUMENT_CTX, YAMLEmitter.ParseContext.MAPPING_CXT,
                                     YAMLEmitter.ParseContext.MAPPING_CXT),
                   equalTo(false));

    }

}