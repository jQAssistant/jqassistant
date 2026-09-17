package com.buschmais.jqassistant.plugin.java.api.report;

import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JavaSourceLocationHelperTest {

    public static final String TYPE_PATH = "src/Test.java";
    public static final String TYPE_FILENAME = "/Test.java";

    public static final String PACKAGE_PATH = "src/com/acme";
    public static final String PACKAGE_FILENAME = "/com/acme";

    @Test
    void classFileDescriptor() {
        ClassFileDescriptor classFileDescriptor = mock(ClassFileDescriptor.class);
        doReturn(TYPE_PATH).when(classFileDescriptor)
            .getPath();
        doReturn(TYPE_FILENAME).when(classFileDescriptor)
            .getFileName();

        Optional<FileLocation> optionalFileLocation = JavaSourceLocationHelper.getSourceLocation(classFileDescriptor, of(1), of(2));

        assertThat(optionalFileLocation).isPresent()
            .hasValueSatisfying(fileLocation -> {
                assertThat(fileLocation.getPath()).isEqualTo(TYPE_PATH);
                assertThat(fileLocation.getFileName()).isEqualTo(TYPE_FILENAME);
                assertThat(fileLocation.getStartLine()).isPresent()
                    .hasValueSatisfying(line -> assertThat(line).isEqualTo(1));
                assertThat(fileLocation.getEndLine()).isPresent()
                    .hasValueSatisfying(line -> assertThat(line).isEqualTo(2));
            });
    }

    @Test
    void typeDescriptor() {
        TypeDescriptor typeDescriptor = mock(TypeDescriptor.class);

        Optional<FileLocation> optionalFileLocation = JavaSourceLocationHelper.getSourceLocation(typeDescriptor, of(1), of(2));

        assertThat(optionalFileLocation).isEmpty();
    }

    @Test
    void packageDescriptor() {
        PackageDescriptor packageDescriptor = mock(PackageDescriptor.class);
        doReturn(PACKAGE_PATH).when(packageDescriptor)
            .getPath();
        doReturn(PACKAGE_FILENAME).when(packageDescriptor)
            .getFileName();

        Optional<FileLocation> optionalFileLocation = JavaSourceLocationHelper.getSourceLocation(packageDescriptor, empty(), empty());

        assertThat(optionalFileLocation).isPresent()
            .hasValueSatisfying(fileLocation -> {
                assertThat(fileLocation.getPath()).isEqualTo(PACKAGE_PATH);
                assertThat(fileLocation.getFileName()).isEqualTo(PACKAGE_FILENAME);
                assertThat(fileLocation.getStartLine()).isNotPresent();
                assertThat(fileLocation.getEndLine()).isNotPresent();
            });
    }
}
