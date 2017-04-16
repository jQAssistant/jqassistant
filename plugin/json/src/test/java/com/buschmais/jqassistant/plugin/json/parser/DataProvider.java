package com.buschmais.jqassistant.plugin.json.parser;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * Centralized provider for all test file collections.
 *
 * The provided test file collections should be used as
 * input for parameterized tests with JUnit.
 */
public class DataProvider {
    public static Collection<Object[]> jsonParsingTestSuiteWithMetaData() throws URISyntaxException {

        /*
         * List of files we accept even if this file should not be accepted according
         * to the "JSON Parsing Test Suite". We didn't remove this files from the
         * file set because to be able to update the test set later.
         */
        List<String> filesWeAccept = asList(
            // Comments are file for us at any position
            "n_object_trailing_comment_slash_open.json",
            "n_object_trailing_comment.json",
            "n_structure_object_with_comment.json"
        );

        List<String> filesToIgnoreTemporarily = asList(
            "i_number_huge_exp.json",
            "i_string_UTF-16LE_with_BOM.json",
            "i_string_utf16BE_no_BOM.json",
            "i_string_utf16LE_no_BOM.json",
            "i_structure_UTF-8_BOM_empty_object.json"
        );

        URL resource = ConfiguredJSONParsingTestSuiteIT.class.getResource("/json_parsing_test_suite");
        File directory = new File(resource.toURI());

        File[] jsons = directory.listFiles(f -> f.isFile() && f.getName().endsWith(".json"));

        return Stream.of(jsons)
            .filter(f -> !filesToIgnoreTemporarily.contains(f.getName()))
            .map(T::new)
            .peek(t -> {
                boolean isAcceptable = t.isAcceptable();
                boolean shouldBeAccepted = filesWeAccept.contains(t.getFile().getName());
                t.setAcceptable(isAcceptable || shouldBeAccepted);
            })
            .map(t -> new Object[]{t})
            .collect(Collectors.toList());
    }


    public static Collection<Object[]> invalidOwnExamples() {
        return Arrays.asList(new Object[][]{
            {"/probes/invalid/empty-file.json"},
            {"/probes/invalid/json-file-as-template.json"}
        });
    }

    public static Collection<Object[]> validOwnExamples() {
        return Arrays.asList(new String[][]{
            {"/probes/valid/array-empty.json"},
            {"/probes/valid/array-one-value.json"},
            {"/probes/valid/array-of-arrays.json"},
            {"/probes/valid/line-comment-before-object.json"},
            {"/probes/valid/line-comment-in-object.json"},
            {"/probes/valid/line-comment-after-object.json"},
            {"/probes/valid/block-comment-in-object.json"},
            {"/probes/valid/true-false-null.json"},
            {"/probes/valid/object-with-objects.json"},
            {"/probes/valid/object-one-key-value-pair.json"},
            {"/probes/valid/object-two-key-value-pairs.json"},
            {"/probes/valid/single-int.json"},
            {"/probes/valid/string-value-with-quote-mark.json"},
            {"/probes/valid/string-value-with-unicode-signs.json"},
            {"/probes/valid/object-with-array-empty.json"},
            {"/probes/valid/object-with-array.json"},
            {"/probes/valid/object-with-array-two-elements.json"},
            {"/probes/valid/object-with-number.json"}
        });
    }

    public static Collection<Object[]> invalidFilesOfJsonParsingTestSuite() throws URISyntaxException {
        Collection<Object[]> basis = jsonParsingTestSuiteWithMetaData();

        List<Object[]> invalidFiles = basis.stream().map(element -> (T) element[0])
                                           .filter(T::isNotAcceptable)
                                           .map(T::getFile)
                                           .map(filePath -> new Object[]{filePath})
                                           .collect(Collectors.toList());

        return invalidFiles;
    }

    static class T {

        private boolean acceptable;
        private File file;

        public T(File f) {
            file = f;
            acceptable = f.getName().startsWith("y_") || f.getName().startsWith("i_");
        }

        public void setAcceptable(boolean isAcceptable) {
            this.acceptable = isAcceptable;
        }

        public boolean isAcceptable() {
            return acceptable;
        }

        public boolean isNotAcceptable() {
            return !isAcceptable();
        }

        public File getFile() {
            return file;
        }
    }
}
