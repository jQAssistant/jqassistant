package com.buschmais.jqassistant.plugins.json.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugins.json.api.model.JSONArrayDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONFileDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONKeyDescriptor;
import com.buschmais.jqassistant.plugins.json.api.model.JSONObjectDescriptor;
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
    public void enterObject(JSONParser.ObjectContext ctx) {
        JSONObjectDescriptor jsonObjectDescriptor = scanner.getContext()
                                                           .getStore()
                                                           .create(JSONObjectDescriptor.class);

        JSONDescriptor descriptor = stack().peek();

        if (descriptor instanceof JSONFileDescriptor) {
            JSONFileDescriptor fileDescriptor = (JSONFileDescriptor) descriptor;
            fileDescriptor.setObject(jsonObjectDescriptor);
        } else if (descriptor instanceof JSONKeyDescriptor) {
            JSONKeyDescriptor parentDescriptor = (JSONKeyDescriptor) stack().peek();
            parentDescriptor.setObject(jsonObjectDescriptor);
        } else {
            throw new IllegalStateException("Unexpected stack state while parsing a JSON document.");
        }

        stack().push(jsonObjectDescriptor);
    }

    @Override
    public void exitObject(JSONParser.ObjectContext ctx) {
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
    public void enterScalarValue(JSONParser.ScalarValueContext ctx) {
        JSONScalarValueDescriptor valueDescriptor = scanner.getContext()
                                                           .getStore()
                                                           .create(JSONScalarValueDescriptor.class);

        JSONDescriptor descriptor = stack().peek();

        if (descriptor instanceof JSONFileDescriptor) {
            ((JSONFileDescriptor) descriptor).setScalarValue(valueDescriptor);
        } else if (descriptor instanceof JSONKeyDescriptor) {
            ((JSONKeyDescriptor) descriptor).setScalarValue(valueDescriptor);
        } else if (descriptor instanceof JSONArrayDescriptor) {
            ((JSONArrayDescriptor) descriptor).getValue().add(valueDescriptor);
        } else {
            throw new IllegalStateException("Internal error. Unexpected top of stack.");
        }

        stack().push(valueDescriptor);
    }

    @Override
    public void exitScalarValue(JSONParser.ScalarValueContext ctx) {
        JSONScalarValueDescriptor valueDescriptor = (JSONScalarValueDescriptor) stack().pop();

        TerminalNode stringNode = ctx.STRING();
        TerminalNode nullNode = ctx.NULL();
        TerminalNode numberNode = ctx.NUMBER();
        TerminalNode boolNode = ctx.BOOLEAN();

        if (stringNode != null) {
            valueDescriptor.setValue(stringNode.getText());
        } else if (nullNode != null) {
            ((JSONKeyDescriptor) stack().peek()).setScalarValue(null);
        } else if (boolNode != null) {
            String textValue = boolNode.getText();
            Boolean boolValue = Boolean.parseBoolean(textValue);
            valueDescriptor.setValue(boolValue);
        } else if (numberNode != null) {
            String textValue = numberNode.getText();
            Double value = Double.parseDouble(textValue);
            double numValue = value.doubleValue();
            valueDescriptor.setValue(numValue);
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
    public void enterArray(JSONParser.ArrayContext ctx) {
        JSONArrayDescriptor jsonArrayDescriptor = scanner.getContext().getStore()
                                                         .create(JSONArrayDescriptor.class);

        JSONDescriptor jsonDescriptor = stack().peek();

        if (jsonDescriptor instanceof JSONFileDescriptor) {
            JSONFileDescriptor fileDescriptor = (JSONFileDescriptor) jsonDescriptor;
            fileDescriptor.setArray(jsonArrayDescriptor);
        } else if (jsonDescriptor instanceof JSONKeyDescriptor) {
            JSONKeyDescriptor keyValueDescriptor = (JSONKeyDescriptor) jsonDescriptor;
            keyValueDescriptor.setArray(jsonArrayDescriptor);
        } else {
            throw new IllegalStateException("Unable to find the context of an JSON array.");
        }

        stack().push(jsonArrayDescriptor);
    }

    @Override
    public void exitArray(JSONParser.ArrayContext ctx) {
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
