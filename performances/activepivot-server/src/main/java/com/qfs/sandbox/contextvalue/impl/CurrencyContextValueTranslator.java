package com.qfs.sandbox.contextvalue.impl;

import com.qfs.sandbox.contextvalue.ACurrencyContextValue;
import com.quartetfs.biz.pivot.context.ContextValueTranslationException;
import com.quartetfs.biz.pivot.context.IContextValueTranslator;
import com.quartetfs.biz.pivot.context.impl.SimpleContextValueTranslator;
import com.quartetfs.fwk.QuartetPluginValue;

import java.util.Collections;
import java.util.Map;


@QuartetPluginValue(intf = IContextValueTranslator.class)
public class CurrencyContextValueTranslator extends SimpleContextValueTranslator<String,ACurrencyContextValue> {

    public static final String KEY = "CURRENCY_CONTEXT";

    @Override
    protected ACurrencyContextValue createInstance(String content) {
        return new CurrencyContextValue(content);
    }

    @Override
    protected String getContent(ACurrencyContextValue currencyContextValue) {
        return currencyContextValue.getCurrency();
    }

    @Override
    public Class<ACurrencyContextValue> getContextInterface() {
        return ACurrencyContextValue.class;
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    protected String format(String content) {
        return content;
    }

    @Override
    protected String parse(String property) throws ContextValueTranslationException {
        if (!Currencies.contains(property)) {
			throw new ContextValueTranslationException("Currency not present in ENUM", key(), property, null);
        }
        return property;
    }


    @Override
    public Map<String, String> getAvailableProperties() {
        return Collections.singletonMap(key(),Currencies.getAvailableCurrencies());
    }
}