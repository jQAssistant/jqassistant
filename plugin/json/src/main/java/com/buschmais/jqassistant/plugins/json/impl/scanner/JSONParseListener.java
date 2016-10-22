package com.buschmais.jqassistant.plugins.json.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugins.json.api.model.JSONArrayDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONArrayValueDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONDocumentDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONFileDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONKeyDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONObjectDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONObjectValueDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONScalarValueDescriptor;
import com.buschmais.jqassistant.plugins.json.impl.parser.JSONBaseListener;
import com.buschmais.jqassistant.plugins.json.impl.parser.JSONParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Stack;

public class JSONParseListener extends JSONBaseListener {

    private final Scanner scanner;
    private final DescriptorStack descriptorStack = new DescriptorStack();

    public JSONParseListener(JSONFileDescriptor fd, Scanner sc) {
        scanner = sc;

        stack().push(fd);
    }

    protected DescriptorStack stack() {
        return descriptorStack;
    }

    @Override
    public void enterJsonDocument(JSONParser.JsonDocumentContext ctx) {
        JSONDocumentDescriptor descriptor = scanner.getContext().getStore()
                                                   .create(JSONDocumentDescriptor.class);

        stack().push(descriptor);
    }

    @Override
    public void exitJsonDocument(JSONParser.JsonDocumentContext ctx) {
        JSONDocumentDescriptor documentDescriptor = (JSONDocumentDescriptor) stack().pop();

        JSONFileDescriptor fileDescriptor = (JSONFileDescriptor) stack().pop();

        if (null != documentDescriptor.getContainer()) {
            fileDescriptor.setDocument(documentDescriptor);
        }
    }

    @Override
    public void enterJsonObject(JSONParser.JsonObjectContext ctx) {
        JSONObjectDescriptor jsonObjectDescriptor = scanner.getContext()
                                                           .getStore()
                                                           .create(JSONObjectDescriptor.class);

        JSONDescriptor descriptor = stack().peek();

        if (descriptor instanceof JSONDocumentDescriptor) {
            JSONDocumentDescriptor documentDescriptor = (JSONDocumentDescriptor) stack().peek();
            documentDescriptor.setContainer(jsonObjectDescriptor);
        } else if (descriptor instanceof JSONObjectValueDescriptor) {
            JSONObjectValueDescriptor parentDescriptor = (JSONObjectValueDescriptor) stack().peek();
            parentDescriptor.setValue(jsonObjectDescriptor);
        } else {
            throw new IllegalStateException("Unexpected stack state while parsing a JSON document.");
        }

        stack().push(jsonObjectDescriptor);
    }

    @Override
    public void exitJsonObject(JSONParser.JsonObjectContext ctx) {
        stack().pop();
    }

    @Override
    public void enterJsonObjectValue(JSONParser.JsonObjectValueContext ctx) {
        JSONObjectValueDescriptor valueDescriptor = scanner.getContext()
                                                           .getStore()
                                                           .create(JSONObjectValueDescriptor.class);

        JSONKeyDescriptor keyDescriptor = (JSONKeyDescriptor) stack().peek();

        keyDescriptor.setValue(valueDescriptor);

        stack().push(valueDescriptor);
    }

    @Override
    public void exitJsonObjectValue(JSONParser.JsonObjectValueContext ctx) {
        super.exitJsonObjectValue(ctx);
        stack().pop();
    }

    @Override
    public void enterKeyValuePair(JSONParser.KeyValuePairContext ctx) {
        JSONKeyDescriptor keyDescriptor = scanner.getContext()
                                                 .getStore()
                                                 .create(JSONKeyDescriptor.class);

        JSONObjectDescriptor jsonContainer = (JSONObjectDescriptor) stack().peek();
        jsonContainer.getKeys().add(keyDescriptor);
        stack().push(keyDescriptor);
    }

    @Override
    public void enterJsonScalarValue(JSONParser.JsonScalarValueContext ctx) {
        JSONScalarValueDescriptor valueDescriptor = scanner.getContext()
                                                           .getStore()
                                                           .create(JSONScalarValueDescriptor.class);

        JSONDescriptor descriptor = stack().peek();

        if (descriptor instanceof JSONKeyDescriptor) {
            ((JSONKeyDescriptor) descriptor).setValue(valueDescriptor);
        } else if (descriptor instanceof JSONArrayDescriptor) {
            ((JSONArrayDescriptor) descriptor).getValues().add(valueDescriptor);
        }

        stack().push(valueDescriptor);
    }

    @Override
    public void exitJsonScalarValue(JSONParser.JsonScalarValueContext ctx) {
        JSONScalarValueDescriptor valueDescriptor = (JSONScalarValueDescriptor) stack().pop();

        TerminalNode stringNode = ctx.STRING();
        TerminalNode nullNode = ctx.NULL();
        TerminalNode numberNode = ctx.NUMBER();
        TerminalNode boolNode = ctx.BOOLEAN();

        if (stringNode != null) {
            valueDescriptor.setValue(stringNode.getText());
        } else if (nullNode != null) {
            ((JSONKeyDescriptor) stack().peek()).setValue(null);
        } else if (boolNode != null) {
            String textValue = boolNode.getText();
            Boolean boolValue = Boolean.parseBoolean(textValue);
            valueDescriptor.setValue(boolValue);
        } else if (numberNode != null) {
            String textValue = numberNode.getText();
            Double value = Double.parseDouble(textValue);
            double intValue = value.doubleValue();
            valueDescriptor.setValue(intValue);
        } else {
            String msg = "Unsupported terminal node for token '" + ctx.getText() + "' found.";
            throw new IllegalStateException(msg);
        }
    }

    @Override
    public void exitKeyValuePair(JSONParser.KeyValuePairContext ctx) {
        String keyName = ctx.STRING().getText();

        JSONKeyDescriptor keyDescriptor = (JSONKeyDescriptor) stack().pop();
        keyDescriptor.setName(keyName);
    }

    @Override
    public void enterJsonArray(JSONParser.JsonArrayContext ctx) {
        JSONArrayValueDescriptor jsonArrayDescriptor = scanner.getContext().getStore()
                                                              .create(JSONArrayValueDescriptor.class);

        JSONDescriptor jsonDescriptor = stack().peek();

        if (jsonDescriptor instanceof JSONDocumentDescriptor) {
            JSONDocumentDescriptor documentDescriptor = (JSONDocumentDescriptor) jsonDescriptor;
            documentDescriptor.setContainer(jsonArrayDescriptor);
        } else if (jsonDescriptor instanceof JSONKeyDescriptor) {
            JSONKeyDescriptor keyValueDescriptor = (JSONKeyDescriptor) jsonDescriptor;
            keyValueDescriptor.setValue(jsonArrayDescriptor);
        } else {
            throw new IllegalStateException("Unable to find the context of an JSON array.");
        }

        stack().push(jsonArrayDescriptor);
    }

    @Override
    public void exitJsonArray(JSONParser.JsonArrayContext ctx) {
        stack().pop();
    }
}

class DescriptorStack {
    private Stack<JSONDescriptor> internal = new Stack<>();

    public void push(JSONDescriptor descriptor) {
        internal.push(descriptor);
    }

    public JSONDescriptor peek() {
        return internal.peek();
    }

    public JSONDescriptor pop() {
        return internal.pop();
    }
}
