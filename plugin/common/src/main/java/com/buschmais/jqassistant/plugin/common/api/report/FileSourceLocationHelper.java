package com.buschmais.jqassistant.plugin.common.api.report;

import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.model.source.ArtifactLocation;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import lombok.NoArgsConstructor;

import static java.util.Optional.*;
import static lombok.AccessLevel.PRIVATE;

/**
 * Provides utility functions for resolving source locations
 * {@link FileDescriptor}s.
 */
@NoArgsConstructor(access = PRIVATE)
public class FileSourceLocationHelper {

    public static Optional<FileLocation> getSourceLocation(FileDescriptor descriptor, Optional<Integer> startLine, Optional<Integer> endLine) {
        return getSourceLocation(descriptor, descriptor, startLine, endLine);
    }

    public static Optional<FileLocation> getSourceLocation(FileDescriptor descriptor, FileDescriptor sourceFileDescriptor, Optional<Integer> startLine,
        Optional<Integer> endLine) {
        String path = sourceFileDescriptor.getPath();
        if (path != null) {
            FileLocation.FileLocationBuilder<?, ?> fileLocationBuilder = FileLocation.builder()
                .path(path)
                .fileName(sourceFileDescriptor.getFileName());
            fileLocationBuilder.startLine(startLine);
            fileLocationBuilder.endLine(endLine);
            fileLocationBuilder.parent(getParentLocation(descriptor));
            return of(fileLocationBuilder.build());
        }
        return empty();
    }

    public static Optional<ArtifactLocation> getParentLocation(FileDescriptor descriptor) {
        for (FileDescriptor parentDescriptor : descriptor.getParents()) {
            if (parentDescriptor instanceof ArtifactFileDescriptor) {
                ArtifactFileDescriptor parentArtifactFileDescriptor = (ArtifactFileDescriptor) parentDescriptor;
                // fileName
                ArtifactLocation.ArtifactLocationBuilder<?, ?> artifactLocationBuilder = ArtifactLocation.builder()
                    .path(parentArtifactFileDescriptor.getPath())
                    .fileName(parentArtifactFileDescriptor.getFileName());
                // optional Maven coordinates
                artifactLocationBuilder.group(ofNullable(parentArtifactFileDescriptor.getGroup()))
                    .name(ofNullable(parentArtifactFileDescriptor.getName()))
                    .version(ofNullable(parentArtifactFileDescriptor.getVersion()))
                    .type(ofNullable(parentArtifactFileDescriptor.getType()))
                    .classifier(ofNullable(parentArtifactFileDescriptor.getClassifier()));
                // optional parent(s)
                artifactLocationBuilder.parent(getParentLocation(parentArtifactFileDescriptor));
                return of(artifactLocationBuilder.build());
            }
        }
        return empty();
    }
}
