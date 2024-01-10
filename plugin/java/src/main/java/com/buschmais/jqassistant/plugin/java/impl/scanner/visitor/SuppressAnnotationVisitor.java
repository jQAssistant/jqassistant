package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.annotation.jQASuppress;
import com.buschmais.jqassistant.plugin.java.api.model.JavaSuppressDescriptor;

import org.objectweb.asm.AnnotationVisitor;

/**
 * {@link AnnotationVisitor} for processing jQASuppress annotations.
 */
class SuppressAnnotationVisitor extends AnnotationVisitor {

    private final JavaSuppressDescriptor suppressDescriptor;
    private String currentAttribute;

    private List<String> suppressIds = new ArrayList<>();

    private String suppressColumn;

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
                break;
            default:
                throw new IllegalArgumentException("Unknown attribute '" + name + "' for " + jQASuppress.class.getName());
            }
        } else {
            switch (currentAttribute) {
            case "value":
                suppressIds.add(value.toString());
                break;
            default:
                throw new IllegalArgumentException("Unknown attribute '" + currentAttribute + "' for " + jQASuppress.class.getName());
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
    }
}
