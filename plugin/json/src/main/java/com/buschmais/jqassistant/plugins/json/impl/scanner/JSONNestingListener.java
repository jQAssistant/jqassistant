package com.buschmais.jqassistant.plugins.json.impl.scanner;

import com.buschmais.jqassistant.plugins.json.impl.parser.JSONBaseListener;
import com.buschmais.jqassistant.plugins.json.impl.parser.JSONParser;

public class JSONNestingListener extends JSONBaseListener {
    private NestingLevelCounter nestingCounter = new NestingLevelCounter(1_000);

    @Override
    public void enterArray(JSONParser.ArrayContext ctx) {
        nestingCounter.enter().check();
    }

    @Override
    public void exitArrayElements(JSONParser.ArrayElementsContext ctx) {
        nestingCounter.leave();
    }

    @Override
    public void enterObject(JSONParser.ObjectContext ctx) {
        nestingCounter.enter().check();
    }

    @Override
    public void exitObject(JSONParser.ObjectContext ctx) {
        nestingCounter.leave();
    }
}
