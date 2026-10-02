package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.report.api.model.SuppressionDescriptor;
import com.buschmais.jqassistant.plugin.java.annotation.jQASuppress;
import com.buschmais.jqassistant.plugin.java.api.model.AnnotatedDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaSuppressDescriptor;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.objectweb.asm.AnnotationVisitor;

/**
 * {@link AnnotationVisitor} for processing jQASuppress annotations.
 */
@Slf4j
class SuppressAnnotationVisitor extends AnnotationVisitor {

    private final AnnotatedDescriptor annotatedDescriptor;

    private final ClassFileVisitorContext classFileVisitorContext;

    private String currentAttribute;

    private final List<String> suppressIds = new ArrayList<>();

    private String suppressColumn;

    private LocalDate suppressUntil;

    private String suppressReason;

    public SuppressAnnotationVisitor(AnnotatedDescriptor annotatedDescriptor, ClassFileVisitorContext classFileVisitorContext) {
        super(ClassFileVisitorContext.ASM_OPCODES);
        this.annotatedDescriptor = annotatedDescriptor;
        this.classFileVisitorContext = classFileVisitorContext;
    }

    @Override
    public void visit(String name, Object value) {
        if (name != null) {
            switch (name) {
            case "column":
                this.suppressColumn = value.toString();
                break;
            case "reason":
                this.suppressReason = value.toString();
                break;
            case "until":
                try {
                    this.suppressUntil = LocalDate.parse(value.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Wrong jQASuppress until date format, must be of format 'yyyy-MM-dd'.", e);
                }
                break;
            default:
                log.warn("Unknown attribute '{}' for {}", name, jQASuppress.class.getName());
            }
        } else {
            switch (currentAttribute) {
            case "value":
                suppressIds.add(value.toString());
                break;
            default:
                log.warn("Unknown attribute '{}' for {}", currentAttribute, jQASuppress.class.getName());
            }
        }
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        this.currentAttribute = name;
        return this;
    }

    @Override
    public void visitEnd() {
        JavaSuppressDescriptor javaSuppressDescriptor = classFileVisitorContext.getStore()
            .addDescriptorType(annotatedDescriptor, JavaSuppressDescriptor.class);
        for (String suppressId : suppressIds) {
            SuppressionDescriptor suppressionDescriptor = getSuppressionDescriptor(javaSuppressDescriptor, suppressId);
            suppressionDescriptor.setColumn(suppressColumn);
            suppressionDescriptor.setUntil(suppressUntil);
            suppressionDescriptor.setReason(suppressReason);
        }
    }

    private @NonNull SuppressionDescriptor getSuppressionDescriptor(JavaSuppressDescriptor javaSuppressDescriptor, String suppressId) {
        return javaSuppressDescriptor.getSuppressions()
            .stream()
            .filter(suppression -> suppressId.equals(suppression.getRuleId()))
            .findAny()
            .orElseGet(() -> {
                SuppressionDescriptor suppressionDescriptor = classFileVisitorContext.getStore()
                    .create(SuppressionDescriptor.class);
                javaSuppressDescriptor.getSuppressions()
                    .add(suppressionDescriptor);
                suppressionDescriptor.setRuleId(suppressId);
                return suppressionDescriptor;
            });
    }
}
