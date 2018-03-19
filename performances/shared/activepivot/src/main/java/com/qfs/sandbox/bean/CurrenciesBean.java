package com.qfs.sandbox.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Iterator;

@Component
public class CurrenciesBean {
    protected static HashSet<String> currencies = new HashSet<>();

    public static void addCurency(String currency) {
        currencies.add(currency);
    }

    public static HashSet<String> getCurrencies() {
        return currencies;
    }

    public static void addCurrency(HashSet<String> c) {
        Iterator<String> it = c.iterator();
        while (it.hasNext())
            currencies.add(it.next());
    }
}
