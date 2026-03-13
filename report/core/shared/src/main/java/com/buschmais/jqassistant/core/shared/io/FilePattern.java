package com.buschmais.jqassistant.core.shared.io;

import java.io.File;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents file pattern consisting of includes and excludes, each of them supporting the wildcards "?" and "*".
 * <p>
 * A matcher evaluates all include patterns before applying the exclude patterns.
 */
@Getter
@Builder
@ToString
public class FilePattern {

    private File directory;

    private Set<String> includes;

    private Set<String> excludes;

}
