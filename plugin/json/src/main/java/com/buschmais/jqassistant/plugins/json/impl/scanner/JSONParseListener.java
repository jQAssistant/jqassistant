package com.buschmais.jqassistant.plugins.json.impl.scanner;

import java.util.Stack;

import com.buschmais.jqassistant.plugins.json.impl.parser.JSONBaseListener;
import com.buschmais.jqassistant.plugins.json.impl.parser.JSONParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugins.json.api.model.*;

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
    public void enterJsonDocument(JSONParser.JsonDocumentContext ctx) {
        JSONDocumentDescriptor descriptor = scanner.getContext().getStore()
                                                   .create(JSONDocumentDescriptor.class);

        stack().push(descriptor);
    }

    @Override
    public void exitJsonDocument(JSONParser.JsonDocumentContext ctx) {
//        JSONDocumentDescriptor documentDescriptor = stack().pop().as(JSONDocumentDescriptor.class);
        JSONDocumentDescriptor documentDescriptor = (JSONDocumentDescriptor) stack().pop();

//        JSONFileDescriptor fileDescriptor = stack().pop().as(JSONFileDescriptor.class);
        JSONFileDescriptor fileDescriptor = (JSONFileDescriptor) stack().pop();

        fileDescriptor.setDocument(documentDescriptor);
    }

    @Override
    public void enterJsonObject(JSONParser.JsonObjectContext ctx) {
        JSONObjectDescriptor jsonObjectDescriptor = scanner.getContext()
                                                           .getStore()
                                                           .create(JSONObjectDescriptor.class);

        //JSONDescriptor descriptor = stack().peek().as(JSONDescriptor.class);
        JSONDescriptor descriptor = stack().peek();

        if (descriptor instanceof JSONDocumentDescriptor) {
//            JSONDocumentDescriptor documentDescriptor = stack().peek().as(JSONDocumentDescriptor.class);
            JSONDocumentDescriptor documentDescriptor = (JSONDocumentDescriptor) stack().peek();
            documentDescriptor.setContainer(jsonObjectDescriptor);
        } else if (descriptor instanceof JSONObjectValueDescriptor){
//            JSONObjectValueDescriptor parentDescriptor = stack().peek().as(JSONObjectValueDescriptor.class);
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
        super.enterJsonObjectValue(ctx);

        try {
            JSONObjectValueDescriptor valueDescriptor = scanner.getContext()
                                                               .getStore()
                                                               .create(JSONObjectValueDescriptor.class);

            JSONKeyDescriptor keyDescriptor = (JSONKeyDescriptor) stack().peek();
//            JSONKeyDescriptor keyDescriptor = stack().peek().as(JSONKeyDescriptor.class);

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
    public void enterKeyValuePair(JSONParser.KeyValuePairContext ctx) {
        JSONKeyDescriptor keyDescriptor = scanner.getContext()
                                                 .getStore()
                                                 .create(JSONKeyDescriptor.class);

//        JSONObjectDescriptor jsonContainer = stack().peek().as(JSONObjectDescriptor.class);
        JSONObjectDescriptor jsonContainer = (JSONObjectDescriptor) stack().peek();
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

//    JSONKeyDescriptor keyDescriptor = stack().peek().as(JSONKeyDescriptor.class);
            JSONKeyDescriptor keyDescriptor = (JSONKeyDescriptor) stack().peek();


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

//        JSONDescriptor descriptor = stack().peek().as(JSONDescriptor.class);
        JSONDescriptor descriptor = stack().peek();

        if (descriptor instanceof JSONKeyDescriptor) {
            ((JSONKeyDescriptor)descriptor).setValue(valueDescriptor);
        } else if (descriptor instanceof JSONArrayDescriptor) {
            ((JSONArrayDescriptor)descriptor).getValues().add(valueDescriptor);
        }

        stack().push(valueDescriptor);
    }

    @Override
    public void exitJsonScalarValue(JSONParser.JsonScalarValueContext ctx) {
//        JSONScalarValueDescriptor valueDescriptor = stack().pop().as(JSONScalarValueDescriptor.class);
        JSONScalarValueDescriptor valueDescriptor = (JSONScalarValueDescriptor) stack().pop();

        TerminalNode stringNode = ctx.STRING();
        TerminalNode nullNode = ctx.NULL();
        TerminalNode boolNode = ctx.BOOLEAN();

        if (stringNode != null) {
            valueDescriptor.setValue(stringNode.getText());
        } else if (nullNode != null) {
            // Null means not value. Therefore we don't need a value descriptor
//            stack().peek().as(JSONKeyDescriptor.class).setValue(null);
            ((JSONKeyDescriptor)stack().peek()).setValue(null);
        } else if (boolNode != null) {
            String textValue = boolNode.getText();
            Boolean boolValue = Boolean.parseBoolean(textValue);
            valueDescriptor.setValue(boolValue);
        }
    }



    @Override
    public void exitKeyValuePair(JSONParser.KeyValuePairContext ctx) {
        String keyName = ctx.STRING().getText();

//        JSONKeyDescriptor keyDescriptor = stack().pop().as(JSONKeyDescriptor.class);
        JSONKeyDescriptor keyDescriptor = (JSONKeyDescriptor) stack().pop();
        keyDescriptor.setName(keyName);
    }

    @Override
    public void enterJsonArray(JSONParser.JsonArrayContext ctx) {
        // JSONArrayDescriptor jsonArrayDescriptor = scanner.getContext()
        // .getStore()
        // .create(JSONArrayDescriptor.class);

        JSONArrayValueDescriptor jsonArrayDescriptor = scanner.getContext().getStore().create(JSONArrayValueDescriptor.class);

//        JSONDescriptor jsonDescriptor = stack().peek().as(JSONDescriptor.class);
        JSONDescriptor jsonDescriptor = stack().peek();

        if (jsonDescriptor instanceof JSONDocumentDescriptor) {
//            JSONDocumentDescriptor documentDescriptor = jsonDescriptor.as(JSONDocumentDescriptor.class);
            JSONDocumentDescriptor documentDescriptor = (JSONDocumentDescriptor) jsonDescriptor;
            documentDescriptor.setContainer(jsonArrayDescriptor);
        } else if (jsonDescriptor instanceof JSONArrayValueDescriptor) {
//            JSONArrayValueDescriptor arrayDescriptor = jsonDescriptor.as(JSONArrayValueDescriptor.class);
            JSONArrayValueDescriptor arrayDescriptor = (JSONArrayValueDescriptor) jsonDescriptor;
            try {
                // arrayDescriptor.setValue(jsonArrayDescriptor);
            } catch (Exception e) {
                System.out.println(e);
                throw e;
            }
        } else {
            throw new IllegalStateException("Unable to find the context of an JSON array.");
        }

        stack().push(jsonArrayDescriptor);
    }

    @Override
    public void exitJsonArray(JSONParser.JsonArrayContext ctx) {
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
