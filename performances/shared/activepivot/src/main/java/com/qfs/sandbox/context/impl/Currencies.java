package com.qfs.sandbox.context.impl;

public enum Currencies {
    EUR("EUR"),
    USD("USD"),
    JPY("JPY"),
    CHF("CHF");

    private String currency;
    Currencies(String c) {
        this.currency = c;
    }

    public static boolean contains(String currency) {
        for (Currencies c : Currencies.values()) {
            if (c.getCurrency().equals(currency)) {
                return true;
            }
        }
        return false;
    }


    public String getCurrency() {
        return currency;
    }


    public static String getAvailableCurrencies() {
        StringBuilder availableCurrencies = new StringBuilder();
        for (Currencies currency : Currencies.values()) {
            availableCurrencies.append(currency.getCurrency()).append(',');
        }
        availableCurrencies.deleteCharAt(availableCurrencies.length() - 1);
        return availableCurrencies.toString();
    }
}
