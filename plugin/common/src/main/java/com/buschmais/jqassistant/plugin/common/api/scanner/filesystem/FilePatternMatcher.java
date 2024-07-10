package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;

/**
 * A matcher for file patterns.
 *
 * Holds patters for file paths to be included or excluded and provides an
 * {@link #accepts(String)} method which evaluates a given path.
 */
public class FilePatternMatcher {

    private Set<String> includeFilePatterns = null;
    private Set<String> excludeFilePatterns = null;

    private FilePatternMatcher() {
    }

    /**
     * Determines if the given path matches the configured include and exclude
     * patterns.
     *
     * NOTE: The include pattern is evaluated before the exclude pattern.
     *
     * @param path
     *            The path.
     * @return <code>true</code> if the path matches.
     */
    public boolean accepts(String path) {
        boolean result;
        if (includeFilePatterns != null) {
            result = matches(path, includeFilePatterns);
        } else {
            result = true;
        }
        if (excludeFilePatterns != null) {
            result = result && !matches(path, excludeFilePatterns);
        }
        return result;
    }

    private boolean matches(String path, Set<String> filePatterns) {
        for (String filePattern : filePatterns) {
            if (FilenameUtils.wildcardMatch(path, filePattern, IOCase.SENSITIVE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return a {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for the file pattern matcher.
     */
    public static class Builder {

        private FilePatternMatcher instance = new FilePatternMatcher();

        /**
         * Configures a list of file name patterns to include.
         *
         * @param patternList
         *            The comma separated list of file name patterns to include.
         * @return The builder.
         */
        public Builder include(String patternList) {
            instance.includeFilePatterns = parse(patternList);
            return this;
        }

        /**
         * Configures a list of file name patterns to exclude.
         *
         * @param patternList
         *            The comma separated list of file name patterns to exclude.
         * @return The builder.
         */
        public Builder exclude(String patternList) {
            instance.excludeFilePatterns = parse(patternList);
            return this;
        }

        /**
         * Returns the configured matcher instance.
         *
         * @return The matcher instance.
         */
        public FilePatternMatcher build() {
            return instance;
        }

        private Set<String> parse(String patternList) {
            if (patternList == null) {
                return null;
            }
            Set<String> patterns = new HashSet<>();
            for (String pattern : patternList.split(",")) {
                String filePattern = pattern.trim();
                if (StringUtils.isNotEmpty(filePattern)) {
                    patterns.add(filePattern);
                }
            }
            return patterns;
        }
    }
}
