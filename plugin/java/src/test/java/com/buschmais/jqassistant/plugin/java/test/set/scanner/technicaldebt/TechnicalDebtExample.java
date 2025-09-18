package com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt;

import com.buschmais.jqassistant.plugin.java.api.annotation.TechnicalDebt;

import static com.buschmais.jqassistant.plugin.java.api.annotation.TechnicalDebt.Priority.LOW;
import static com.buschmais.jqassistant.plugin.java.api.annotation.TechnicalDebt.Priority.MEDIUM;

@TechnicalDebt(value = "technicalDebt:Class", priority = MEDIUM, issue = "Description why this class is a debt.")
public class TechnicalDebtExample {

    @TechnicalDebt(value = "technicalDebt:Method", priority = LOW, issue = "Description why this field is problematic.")
    private String debtField;

    @TechnicalDebt(value = "technicalDebt:Method", priority = LOW, issue = "Description why field is broken.")
    public void doSomething() {
    }
}


