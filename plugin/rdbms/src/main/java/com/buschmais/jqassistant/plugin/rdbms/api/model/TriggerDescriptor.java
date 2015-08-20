package com.buschmais.jqassistant.plugin.rdbms.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Trigger")
public interface TriggerDescriptor extends RdbmsDescriptor, NamedDescriptor {

    String getActionCondition();

    void setActionCondition(String actionCondition);

    int getActionOrder();

    void setActionOrder(int actionOrder);

    String getActionOrientation();

    void setActionOrientation(String actionOrientation);

    String getActionStatement();

    void setActionStatement(String actionStatement);

    String getConditionTiming();

    void setConditionTiming(String conditionTiming);

    String getEventManipulationTime();

    void setEventManipulationTime(String eventManipulationTime);
}
