package com.qfs.sandbox.bean;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;


@Component // spring component who lives during server excecution
public class CurrenciesBean {

    private static final long serialVersionUID = 2018042711452L;

    protected static HashSet<String> currencies = new HashSet<>();

    public static void addCurrency(String currency){
        currencies.add(currency);
    }

    public static HashSet<String> getCurrencies(){
        return currencies;
    }

    public static void addCurrency(HashSet<String> currenciesToAdd){
        currenciesToAdd.forEach(c ->
            currencies.add(c)
        );
    }
}
