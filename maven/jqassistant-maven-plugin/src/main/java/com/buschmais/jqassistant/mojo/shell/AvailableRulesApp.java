package com.buschmais.jqassistant.mojo.shell;

import org.neo4j.helpers.Service;
import org.neo4j.shell.*;
import org.neo4j.shell.impl.AbstractApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service.Implementation(App.class)
public class AvailableRulesApp extends AbstractApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvailableRulesApp.class);

    public AvailableRulesApp() {
        LOGGER.debug("initialize available rules");
    }

    @Override
    public Continuation execute(AppCommandParser parser, Session session, Output out) throws Exception {
        return Continuation.INPUT_COMPLETE;
    }
}
