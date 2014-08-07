package com.buschmais.jqassistant.examples.rules.forbiddenmethod;

import java.util.Date;

/**
 * Uses the a deprecated constructor of {@link java.util.Date} which shall be
 * forbidden to be invoked.
 */
public class MyService {

    @SuppressWarnings("deprecation")
    public Date determineDate(int year, int month, int day) {
        return new Date(year, month, day);
    }
}
