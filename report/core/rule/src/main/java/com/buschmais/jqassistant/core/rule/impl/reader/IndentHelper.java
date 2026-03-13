package com.buschmais.jqassistant.core.rule.impl.reader;

public class IndentHelper {

    private IndentHelper() {
    }

    /**
     * Removes blanks from the given text
     * <ul>
     *     <li>leading and trailing blank lines</li>
     *     <li>indent consisting of blank characters that is equal in all lines</li>
     * </ul>
     *
     * @param text The text.
     * @return The without blanks
     */
    public static String removeIndent(String text) {
        if (text == null) {
            return null;
        }
        String textWithoutEmptyLines = removeBlankLeadingAndTrailingLines(text);
        String[] lines = textWithoutEmptyLines.split("\\n");
        if (text.isBlank()) {
            return textWithoutEmptyLines;
        }
        int indent = getIndent(lines);
        return removeIndent(lines, indent);
    }

    /**
     * Removes leading and traling empty lines from the given text.
     *
     * @param text
     *     The text.
     * @return The text without trailing and leading  empty lines.
     */
    private static String removeBlankLeadingAndTrailingLines(String text) {
        String[] lines = text.split("\\n");

        // read the text from the top and give it a value, so we can print from this value.
        int startIndex = 0;
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].isBlank()) {
                startIndex = i;
                break;
            }
        }

        // opposite to the last reader , this reads the text from the bottom to top, also give it a value.
        int lastIndex = lines.length - 1;
        for (int i = lines.length - 1; i >= 0; i--) {
            if (!lines[i].isBlank()) {
                lastIndex = i;
                break;
            }
        }
        // Here we print the text which is between startIndex and lastIndex, and with that we lose the empty leading trailing lines.
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = startIndex; i <= lastIndex; i++) {
            resultBuilder.append(lines[i]);
            if (i < lastIndex) {
                resultBuilder.append("\n");
            }
        }
        return resultBuilder.toString();
    }

    /**
     * Determines the number columns that are represent identical indentation used in all lines of the text.
     * <p>
     * Blank lines are not considered for calculation.
     *
     * @param lines
     *     The lines.
     * @return The count of columns which represent the indent.
     */
    private static int getIndent(String[] lines) {
        int currentColumn = 0;
        while (true) {
            String prevChar = null;
            for (String line : lines) {
                if (!line.isBlank()) {
                    if (currentColumn == line.length()) {
                        return currentColumn;
                    }
                    String currentChar = line.substring(currentColumn, currentColumn + 1);
                    if (!currentChar.isBlank() || (prevChar != null && !prevChar.equals(currentChar))) {
                        return currentColumn;
                    }
                    prevChar = currentChar;
                }
            }
            currentColumn++;
        }
    }

    /**
     * Removes the specified indentation from each line in the provided array of lines and strips the trailing blanks from each line.
     * @param lines  The lines.
     * @param indent The number of leading whitespace characters to be removed from each line
     * @return The text with indentation removed
     */
    private static String removeIndent(String[] lines, int indent) {
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            // We replace all trailing whitespaces from each line.
            String line = lines[i].replaceAll("\\s+$", "");
            if (!line.isBlank()) {
                resultBuilder.append(line.substring(indent));
            } else {
                resultBuilder.append(lines[i]);
            }
            if (i < lines.length - 1) {
                resultBuilder.append("\n");
            }
        }
        return resultBuilder.toString();

    }
}
