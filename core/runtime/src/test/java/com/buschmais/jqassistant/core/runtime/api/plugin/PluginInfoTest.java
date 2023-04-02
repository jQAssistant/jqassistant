package com.buschmais.jqassistant.core.runtime.api.plugin;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static com.buschmais.jqassistant.core.runtime.api.plugin.PluginInfo.ID_COMPARATOR;
import static com.buschmais.jqassistant.core.runtime.api.plugin.PluginInfo.NAME_COMPARATOR;
import static org.assertj.core.api.Assertions.assertThat;

class PluginInfoTest {
    @Nested
    class NameComparator {
        @ParameterizedTest
        @CsvFileSource(resources = "/testdata/plugin-info-data.left-less-then-right.csv", nullValues = "N/A", numLinesToSkip = 1)
        void comparisonWorksIfBothAreDifferent(String leftName, String leftId, String rightName, String rightId) {
            PluginInfo left = new MyPluginInfo(leftName, leftId);
            PluginInfo right = new MyPluginInfo(rightName, rightId);

            assertThat(NAME_COMPARATOR.compare(left, right)).isLessThanOrEqualTo(-1);
            assertThat(NAME_COMPARATOR.compare(right, left)).isGreaterThanOrEqualTo(1);
            assertThat(NAME_COMPARATOR.compare(left, left)).isEqualTo(0);
            assertThat(NAME_COMPARATOR.compare(right, right)).isEqualTo(0);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/testdata/plugin-info-data.left-and-right-are-equal.csv", nullValues = "N/A", numLinesToSkip = 1)
        void comparisonWorksIfBothAreEqual(String leftName, String leftId, String rightName, String rightId) {
            PluginInfo left = new MyPluginInfo(leftName, leftId);
            PluginInfo right = new MyPluginInfo(rightName, rightId);

            assertThat(NAME_COMPARATOR.compare(left, right)).isEqualTo(0);
            assertThat(NAME_COMPARATOR.compare(right, left)).isEqualTo(0);
            assertThat(NAME_COMPARATOR.compare(left, left)).isEqualTo(0);
            assertThat(NAME_COMPARATOR.compare(right, right)).isEqualTo(0);
        }
    }

    @Nested
    class IdComparator {
        @ParameterizedTest
        @CsvFileSource(resources = "/testdata/plugin-info-data.left-less-then-right.csv", nullValues = "N/A", numLinesToSkip = 1)
        void comparisonWorksIfBothAreDifferent(String leftName, String leftId, String rightName, String rightId) {
            PluginInfo left = new MyPluginInfo(leftName, leftId);
            PluginInfo right = new MyPluginInfo(rightName, rightId);

            assertThat(ID_COMPARATOR.compare(left, right)).isLessThanOrEqualTo(-1);
            assertThat(ID_COMPARATOR.compare(right, left)).isGreaterThanOrEqualTo(1);
            assertThat(ID_COMPARATOR.compare(left, left)).isEqualTo(0);
            assertThat(ID_COMPARATOR.compare(right, right)).isEqualTo(0);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/testdata/plugin-info-data.left-and-right-are-equal.csv", nullValues = "N/A", numLinesToSkip = 1)
        void comparisonWorksIfBothAreEqual(String leftName, String leftId, String rightName, String rightId) {
            PluginInfo left = new MyPluginInfo(leftName, leftId);
            PluginInfo right = new MyPluginInfo(rightName, rightId);

            assertThat(ID_COMPARATOR.compare(left, right)).isEqualTo(0);
            assertThat(ID_COMPARATOR.compare(right, left)).isEqualTo(0);
            assertThat(ID_COMPARATOR.compare(left, left)).isEqualTo(0);
            assertThat(ID_COMPARATOR.compare(right, right)).isEqualTo(0);
        }
    }

    private static class MyPluginInfo implements PluginInfo {
        String name;
        String id;

        public MyPluginInfo(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }
    }
}
