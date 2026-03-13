package com.buschmais.jqassistant.core.rule.impl.reader;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.rule.impl.reader.IndentHelper.removeIndent;
import static org.assertj.core.api.Assertions.assertThat;

class IndentHelperTest {

    @Test
    void leadingAndTrailingSpaces() {
        assertThat(removeIndent("  text  ")).isEqualTo("text");
    }

    @Test
    void multiLineText() {
        assertThat(removeIndent("text\ntext")).isEqualTo("text\ntext");
    }

    @Test
    void removeLeadingAndTrailingBlankLines() {
        assertThat(removeIndent("  \n text to believe\ntext to work \n  ")).isEqualTo(" text to believe\ntext to work");
    }

    @Test
    void emptyText() {
        assertThat(removeIndent(null)).isNull();
        assertThat(removeIndent("")).isEmpty();
        assertThat(removeIndent("\n")).isEmpty();
    }

    @Test
    void moveTextToLeftWithEmptyLineInMiddle() {
        assertThat(removeIndent("  text    text\n\n  text    text")).isEqualTo("text    text\n\ntext    text");
        assertThat(removeIndent("  text    text\n\n\n  text")).isEqualTo("text    text\n\n\ntext");
    }

    @Test
    void removeIndentUsingSpace() {
        assertThat(removeIndent(" text")).isEqualTo("text");
        assertThat(removeIndent("  text")).isEqualTo("text");
        assertThat(removeIndent("    text")).isEqualTo("text");
        assertThat(removeIndent(" \n    text\n  ")).isEqualTo("text");
        assertThat(removeIndent(" \n    text   \n")).isEqualTo("text");
    }

    @Test
    void textTab() {
        assertThat(removeIndent("\ttext")).isEqualTo("text");
        assertThat(removeIndent("\t  text  ")).isEqualTo("text");
        assertThat(removeIndent("\t\ttext")).isEqualTo("text");
    }

    @Test
    void multiLineTextTabLines() {
        assertThat(removeIndent("\ttext\n\ttext")).isEqualTo("text\ntext");
        assertThat(removeIndent("\t\ttext\n\t\ttext")).isEqualTo("text\ntext");
        assertThat(removeIndent("text\n\ttext")).isEqualTo("text\n\ttext");
        assertThat(removeIndent("\ttext\n\t\ttext")).isEqualTo("text\n\ttext");
        assertThat(removeIndent("\ttext\ntext")).isEqualTo("\ttext\ntext");
    }

    @Test
    void multiLineTextTabWithSpaces() {
        assertThat(removeIndent("text \n\t text")).isEqualTo("text\n\t text");
        assertThat(removeIndent("\t  text\n\t  text")).isEqualTo("text\ntext");
        assertThat(removeIndent("  \ttext\n  \ttext")).isEqualTo("text\ntext");


    }

    @Test
    void multiLineTextTabWithNeededSpaces() {
        assertThat(removeIndent("\ttext\n\t  text")).isEqualTo("text\n  text");
        assertThat(removeIndent("\ttext\n\t  text")).isEqualTo("text\n  text");
        assertThat(removeIndent("\t  text  \ttext\n\t  text")).isEqualTo("text  \ttext\ntext");
        assertThat(removeIndent("\t  text\n\t  text  \ttext")).isEqualTo("text\ntext  \ttext");
    }

    @Test
    void multiLineTextTabEmptyLineBetween() {
        assertThat(removeIndent("\ttext\n\n\ttext")).isEqualTo("text\n\ntext");
    }

    @Test
    void unorderedTabs() {
        assertThat(removeIndent("  \ttext\n\t   text")).isEqualTo("  \ttext\n\t   text");
        assertThat(removeIndent("\t  text\n  \ttext ")).isEqualTo("\t  text\n  \ttext");
        assertThat(removeIndent("\t  text\n  \ttext \t  text\n  \ttext")).isEqualTo("\t  text\n  \ttext \t  text\n  \ttext");
    }
}
