package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.annotation.jQASuppress;
import com.buschmais.jqassistant.plugin.java.api.model.JavaSuppressDescriptor;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.AnnotationVisitor;

/**
 * {@link AnnotationVisitor} for processing jQASuppress annotations.
 */
@Slf4j
class SuppressAnnotationVisitor extends AnnotationVisitor {

    private final JavaSuppressDescriptor suppressDescriptor;
    private String currentAttribute;

    private List<String> suppressIds = new ArrayList<>();

    private String suppressColumn;
    private LocalDate suppressUntil;
    private String suppressReason;

    public SuppressAnnotationVisitor(JavaSuppressDescriptor suppressDescriptor) {
        super(VisitorHelper.ASM_OPCODES);
        this.suppressDescriptor = suppressDescriptor;
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
                }
                catch (DateTimeParseException e){
                    throw new IllegalArgumentException("Wrong jQASuppress until date format, must be of format 'yyyy-MM-dd'.", e );
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
        suppressDescriptor.setSuppressIds(suppressIds.toArray(new String[suppressIds.size()]));
        suppressDescriptor.setSuppressColumn(suppressColumn);
        suppressDescriptor.setSuppressUntil(suppressUntil);
        suppressDescriptor.setSuppressReason(suppressReason);
    }
}
