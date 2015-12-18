package com.buschmais.jqassistant.plugin.json.impl.scanner;

import java.util.Stack;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.jqassistant.plugin.json.parser.JSONBaseListener;
import org.jqassistant.plugin.json.parser.JSONParser;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.json.api.model.*;

public class JSONParseListener extends JSONBaseListener {

    private final Scanner scanner;
    private final Stack<JSONDescriptor> descriptorStack = new Stack<>();

    public JSONParseListener(JSONFileDescriptor fd, Scanner sc) {
        scanner = sc;

        stack().push(fd);
    }

    protected Stack<JSONDescriptor> stack() {
        return descriptorStack;
    }

    @Override
    public void enterJsonDocument(org.jqassistant.plugin.json.parser.JSONParser.JsonDocumentContext ctx) {
        JSONDocumentDescriptor descriptor = scanner.getContext().getStore()
                                                   .create(JSONDocumentDescriptor.class);

        stack().push(descriptor);
    }

    @Override
    public void exitJsonDocument(org.jqassistant.plugin.json.parser.JSONParser.JsonDocumentContext ctx) {
        JSONDocumentDescriptor documentDescriptor = stack().pop().as(JSONDocumentDescriptor.class);
        JSONFileDescriptor fileDescriptor = stack().pop().as(JSONFileDescriptor.class);

        fileDescriptor.setDocument(documentDescriptor);
    }

    @Override
    public void enterJsonObject(org.jqassistant.plugin.json.parser.JSONParser.JsonObjectContext ctx) {
        JSONObjectDescriptor jsonObjectDescriptor = scanner.getContext()
                                                           .getStore()
                                                           .create(JSONObjectDescriptor.class);

        JSONDescriptor descriptor = stack().peek().as(JSONDescriptor.class);

        if (descriptor instanceof JSONDocumentDescriptor) {
            JSONDocumentDescriptor documentDescriptor = stack().peek().as(JSONDocumentDescriptor.class);
            documentDescriptor.setContainer(jsonObjectDescriptor);
        } else if (descriptor instanceof JSONObjectValueDescriptor){
            JSONObjectValueDescriptor parentDescriptor = stack().peek().as(JSONObjectValueDescriptor.class);
            parentDescriptor.setValue(jsonObjectDescriptor);
        } else {
            throw new IllegalStateException("Unexpected stack state while parsing a JSON document.");
        }

        stack().push(jsonObjectDescriptor);
    }

    @Override
    public void exitJsonObject(org.jqassistant.plugin.json.parser.JSONParser.JsonObjectContext ctx) {
        stack().pop();
    }

    @Override
    public void enterJsonObjectValue(JSONParser.JsonObjectValueContext ctx) {
        super.enterJsonObjectValue(ctx);

        try {
            JSONObjectValueDescriptor valueDescriptor = scanner.getContext()
                                                               .getStore()
                                                               .create(JSONObjectValueDescriptor.class);

            JSONKeyDescriptor keyDescriptor = stack().peek().as(JSONKeyDescriptor.class);

            keyDescriptor.setValue(valueDescriptor);

            stack().push(valueDescriptor);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("******");
    }

    @Override
    public void exitJsonObjectValue(JSONParser.JsonObjectValueContext ctx) {
        super.exitJsonObjectValue(ctx);
        stack().pop();
    }

    @Override
    public void enterKeyValuePair(org.jqassistant.plugin.json.parser.JSONParser.KeyValuePairContext ctx) {
        JSONKeyDescriptor keyDescriptor = scanner.getContext()
                                                 .getStore()
                                                 .create(JSONKeyDescriptor.class);

        JSONObjectDescriptor jsonContainer = stack().peek().as(JSONObjectDescriptor.class);
        jsonContainer.getKeys().add(keyDescriptor);
        stack().push(keyDescriptor);
    }

    @Override
    public void exitJsonArrayValue(JSONParser.JsonArrayValueContext ctx) {
        System.out.println("Popping Json Array Value Descriptor");
        stack().pop();

    }

    @Override
    public void enterJsonArrayValue(JSONParser.JsonArrayValueContext ctx) {
try {
    JSONArrayValueDescriptor valueDescriptor = scanner.getContext()
                                                      .getStore()
                                                      .create(JSONArrayValueDescriptor.class);

    JSONKeyDescriptor keyDescriptor = stack().peek().as(JSONKeyDescriptor.class);


    System.out.println("Pushing Json Array Value Descriptor");
    keyDescriptor.setValue(valueDescriptor);

    stack().push(valueDescriptor);

} catch (Exception e) {
    System.out.println(e);
}
    }

    @Override
    public void enterJsonScalarValue(JSONParser.JsonScalarValueContext ctx) {
        JSONScalarValueDescriptor valueDescriptor = scanner.getContext()
                                                           .getStore()
                                                           .create(JSONScalarValueDescriptor.class);

        JSONDescriptor descriptor = stack().peek().as(JSONDescriptor.class);

        if (descriptor instanceof JSONKeyDescriptor) {
            ((JSONKeyDescriptor)descriptor).setValue(valueDescriptor);
        } else if (descriptor instanceof JSONArrayDescriptor) {
            ((JSONArrayDescriptor)descriptor).getValues().add(valueDescriptor);
        }

        stack().push(valueDescriptor);
    }

    @Override
    public void exitJsonScalarValue(JSONParser.JsonScalarValueContext ctx) {
        JSONScalarValueDescriptor valueDescriptor = stack().pop().as(JSONScalarValueDescriptor.class);

        TerminalNode stringNode = ctx.STRING();
        TerminalNode nullNode = ctx.NULL();
        TerminalNode boolNode = ctx.BOOLEAN();

        if (stringNode != null) {
            valueDescriptor.setValue(stringNode.getText());
        } else if (nullNode != null) {
            // Null means not value. Therefore we don't need a value descriptor
            stack().peek().as(JSONKeyDescriptor.class).setValue(null);
        } else if (boolNode != null) {
            String textValue = boolNode.getText();
            Boolean boolValue = Boolean.parseBoolean(textValue);
            valueDescriptor.setValue(boolValue);
        }
    }



    @Override
    public void exitKeyValuePair(org.jqassistant.plugin.json.parser.JSONParser.KeyValuePairContext ctx) {
        String keyName = ctx.STRING().getText();

        JSONKeyDescriptor keyDescriptor = stack().pop().as(JSONKeyDescriptor.class);
        keyDescriptor.setName(keyName);
    }

    @Override
    public void enterJsonArray(org.jqassistant.plugin.json.parser.JSONParser.JsonArrayContext ctx) {
         JSONArrayDescriptor jsonArrayDescriptor = scanner.getContext()
         .getStore()
         .create(JSONArrayDescriptor.class);

//        JSONArrayValueDescriptor jsonArrayDescriptor = scanner.getContext().getStore().create(JSONArrayValueDescriptor.class);

        JSONDescriptor jsonDescriptor = stack().peek().as(JSONDescriptor.class);

        if (jsonDescriptor instanceof JSONDocumentDescriptor) {
            JSONDocumentDescriptor documentDescriptor = jsonDescriptor.as(JSONDocumentDescriptor.class);
            documentDescriptor.setContainer(jsonArrayDescriptor);
        } else if (jsonDescriptor instanceof JSONArrayValueDescriptor) {
            JSONArrayValueDescriptor arrayDescriptor = jsonDescriptor.as(JSONArrayValueDescriptor.class);
            try {
                // arrayDescriptor.setValue(jsonArrayDescriptor);
            } catch (Exception e) {
                System.out.println(e);
                throw e;
            }
        } else {
            // @todo throw new IllegalStateException("Unable to find the context of an JSON array.");
            System.out.println("Unable to find the context of an JSON array. Peng!");
            System.exit(10);
        }

        stack().push(jsonArrayDescriptor);
    }

    @Override
    public void exitJsonArray(org.jqassistant.plugin.json.parser.JSONParser.JsonArrayContext ctx) {
        stack().pop();
    }

//    @Override
//    public void enterValue(org.jqassistant.plugin.json.parser.JSONParser.ValueContext ctx) {
//        JSONValueDescriptor valueDescriptor = scanner.getContext().getStore().create(JSONValueDescriptor.class);
//
//        JSONDescriptor keyDescriptor = stack().peek().as(JSONDescriptor.class);
//
//        if (keyDescriptor instanceof JSONKeyDescriptor) {
//            ((JSONKeyDescriptor)keyDescriptor).setValue(valueDescriptor);
//        } else {
//            ((JSONArrayDescriptor)keyDescriptor).getValues().add(valueDescriptor);
//        }
//
//        stack().push(valueDescriptor);
//    }

//    @Override
//    public void exitValue(org.jqassistant.plugin.json.parser.JSONParser.ValueContext ctx) {
//
//         There might be a better way to figure out what kind of value we have.
        // Feel free to improved it! Oliver B. Fischer, 2015-10-22
//        JSONValueDescriptor valueDescriptor = stack().pop().as(JSONValueDescriptor.class);
//
//        TerminalNode stringNode = ctx.STRING();
//        TerminalNode numberNode = ctx.NUMBER();
//        TerminalNode booleanNode = ctx.BOOLEAN();
//        TerminalNode nullNode = ctx.NULL();
//
//        if (stringNode != null) {
//            String stringValue = stringNode.getText();
//            valueDescriptor.setValue(stringValue);
//        } else if (booleanNode != null) {
//            valueDescriptor.setValue(Boolean.parseBoolean(booleanNode.getSymbol().getText()));
//        } else if (nullNode != null) {
//             If there is no value we do not need a value descriptor at all
//            JSONKeyDescriptor keyDescriptor = stack().peek().as(JSONKeyDescriptor.class);
//            keyDescriptor.setValue(null);
//        } else {
//            throw new IllegalStateException("Unable to handle the value assigned to a JSON key.");
//        }
//    }
}
