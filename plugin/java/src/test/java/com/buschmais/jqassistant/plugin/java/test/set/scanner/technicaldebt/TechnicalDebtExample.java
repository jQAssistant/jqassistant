package com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt;

import com.buschmais.jqassistant.plugin.java.api.annotation.TechnicalDebt;

import static com.buschmais.jqassistant.plugin.java.api.annotation.TechnicalDebt.Priority.*;

@TechnicalDebt("technicalDebt:Class with defaults")
@TechnicalDebt(value = "technicalDebt:Class with debts", priority = MEDIUM, issue = "284")
public class TechnicalDebtExample {

    @TechnicalDebt(value = "technicalDebt:Field with debts", priority = HIGH, issue = "859")
    private String debtField;

    @TechnicalDebt(value = "technicalDebt:Method with debts", priority = LOW, issue = "3")
    public void doSomething() {
    }
}


