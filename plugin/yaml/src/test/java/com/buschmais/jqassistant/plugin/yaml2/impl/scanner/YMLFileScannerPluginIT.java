package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "jqassistant.yaml2.activate", matches = "^true$")
class YMLFileScannerPluginIT extends AbstractPluginIT {

    @BeforeEach
    void startTransaction() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTransaction() {
        store.commitTransaction();
    }



    @Nested
    class SimpleTests {
        @Test
        void canSimplyProcessASimpleMappingWithoutPeggingOut() {
            File yamlFile = new File(getClassesDirectory(YMLFileScannerPlugin.class), "/simple/simple-001.yaml");

            YMLFileDescriptor result = getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

            assertThat(result).isNotNull();
        }

        @Disabled("Test and scanner are not yet implemented.")
        @Test
        void parsesFileWithMultipleEmptyDocuments() {
            File yamlFile = new File(getClassesDirectory(YMLFileScannerPlugin.class),
                                     "/simple/simple-010-multiple-empty-documents.yaml");

            YMLFileDescriptor result = getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

            assertThat(result).isNotNull();

            /*

            ---
            ---
            ---

            todo Das Ergebnis als Stream. Warum gibt es ein VAL event?
            // see https://bitbucket.org/asomov/snakeyaml-engine/issues/12/

            +STR
            +DOC ---
            =VAL :
            -DOC
            +DOC ---
            =VAL :
            -DOC
            +DOC ---
            =VAL :
            -DOC
            -STR
             */

            Assertions.fail("This test has not been finished.");
        }
    }

    @Nested
    class SimpleSequenceMappings {
        @Disabled("Test and scanner are not yet implemented.")
        @Test
        void canProcessASequenceOfStrings() {
            File yamlFile = new File(getClassesDirectory(YMLFileScannerPlugin.class), "/simple/simple-100-sequence-of-strings.yaml");

            YMLFileDescriptor result = getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

            assertThat(result).isNotNull();



            Assertions.fail("This test has not been finished yet.");
        }
    }

    @Nested
    class ComplexSequenceMappings {
    }


    @Nested
    class SimpleMapMappings {
        @Disabled("Test and scanner are not yet implemented.")
        @Test
        void canProcessAMapOfStrings() {
            File yamlFile = new File(getClassesDirectory(YMLFileScannerPlugin.class), "/simple/simple-200-map-of-strings.yaml");

            YMLFileDescriptor result = getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

            assertThat(result).isNotNull();



            Assertions.fail("This test has not been finished yet.");
        }

    }

    @Nested
    class ComplexMapMappings {
    }


}
