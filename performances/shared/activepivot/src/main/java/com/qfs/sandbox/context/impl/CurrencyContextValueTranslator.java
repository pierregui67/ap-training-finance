package com.qfs.sandbox.context.impl;

import com.qfs.sandbox.context.ICurrencyContextValue;

import com.quartetfs.biz.pivot.context.ContextValueTranslationException;
import com.quartetfs.biz.pivot.context.impl.SimpleContextValueTranslator;

import java.util.Collections;
import java.util.Map;

public class CurrencyContextValueTranslator extends SimpleContextValueTranslator<String, ICurrencyContextValue> {


    public static final String KEY = "CURRENCY_CONTEXT";


    @Override
    protected String format(String s) {
        return s;
    }

    @Override
    protected String parse(String s) throws ContextValueTranslationException {
        if (!Currencies.contains(s)) {
            throw new ContextValueTranslationException("Currency not present in ENUM", key(), s, null);
        }
        return s;
    }

    @Override
    protected ICurrencyContextValue createInstance(String s) {
        return new CurrencyContextValue(s);
    }

    @Override
    protected String getContent(ICurrencyContextValue currencyContextValue) {
        return currencyContextValue.getCurrency();
    }

    @Override
    public Class<ICurrencyContextValue> getContextInterface() {
        return ICurrencyContextValue.class;
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public Map<String, String> getAvailableProperties() {
        return Collections.singletonMap(key(), Currencies.getAvailableCurrencies());
    }
}
