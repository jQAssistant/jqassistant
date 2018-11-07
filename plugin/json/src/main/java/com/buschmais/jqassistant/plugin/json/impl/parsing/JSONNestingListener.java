package com.buschmais.jqassistant.plugin.json.impl.parsing;

import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONBaseListener;
import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONParser;

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
