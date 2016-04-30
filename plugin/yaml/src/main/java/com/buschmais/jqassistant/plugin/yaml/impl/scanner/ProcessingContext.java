package com.buschmais.jqassistant.plugin.yaml.impl.scanner;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLKeyDescriptor;

class ProcessingContext {
    private ArrayDeque<YAMLDescriptor> stackedContext = new ArrayDeque<>();
    private LinkedList<YAMLEmitter.ParseContext> context = new LinkedList<>();

    public void push(YAMLDescriptor newContext) {
        stackedContext.push(newContext);
    }

    @SuppressWarnings("unchecked")
    public <T extends YAMLDescriptor> T peek() {
        return (T)stackedContext.peek();
    }

    @SuppressWarnings("unchecked")
    public <T extends YAMLDescriptor> T pop() {
        return (T)stackedContext.pop();
    }

    public void pushContextEvent(YAMLEmitter.ParseContext event) {
        context.push(event);
    }

    public YAMLEmitter.ParseContext getContext() {
        return context.peek();
    }

    public boolean isContext(YAMLEmitter.ParseContext... eventChain) {
        int pathLength = eventChain.length;
        int currentContextDepth = context.size();

        boolean result = true;

        if (pathLength <= currentContextDepth) {
            List<YAMLEmitter.ParseContext> tail = context.subList(0, pathLength);

            for (int i = pathLength - 1; i >= 0; i--) {
                if (eventChain[i] != tail.get(pathLength - 1 - i)) {
                    result = false;
                    break;
                }
            }
        } else {
            result = false;
        }

        return result;
    }

    public void popContextEvent(int elements) {
        for (int i = 0; i < elements; i++) {
            context.pop();
        }
    }

    public String buildNextFQN(String lastElement) {
        StringBuilder builder = new StringBuilder();

        Iterator<YAMLDescriptor> descItr = stackedContext.descendingIterator();

        while (descItr.hasNext()) {
            YAMLDescriptor yamld = descItr.next();

            if (YAMLKeyDescriptor.class.isAssignableFrom(yamld.getClass())) {
                YAMLKeyDescriptor keyDescriptor = (YAMLKeyDescriptor) yamld;
                builder.append(keyDescriptor.getName()).append('.');
            }
        }

        String name = builder.append(lastElement).toString();

        return name;
    }
}
