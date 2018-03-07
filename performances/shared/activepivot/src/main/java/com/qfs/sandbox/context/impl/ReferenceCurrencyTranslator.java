/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.context.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import com.qfs.sandbox.context.IReferenceCurrency;
import com.quartetfs.biz.pivot.context.ContextValueTranslationException;
import com.quartetfs.biz.pivot.context.IContextValueTranslator;
import com.quartetfs.biz.pivot.context.impl.SimpleContextValueTranslator;
import com.quartetfs.fwk.QuartetPluginValue;

/**
 *
 * Context value translator for reference currencies.
 * As a plugin value it is automatically registered
 * with the other available context value translators.
 *
 * @author Quartet FS
 *
 */
@QuartetPluginValue(intf=IContextValueTranslator.class)
public class ReferenceCurrencyTranslator extends SimpleContextValueTranslator<String, IReferenceCurrency> {

    /** serialVersionUID */
    private static final long serialVersionUID = -1129088664293462391L;

    protected HashSet<String> currencies = new HashSet<String>();

    /** Translator key */
    public static final String KEY = "referenceCurrency";

    public ReferenceCurrencyTranslator() {
        super();
        currencies.add("EUR");
        currencies.add("USD");
        currencies.add("JPY");
        currencies.add("GBP");
        currencies.add("ZAR");
        currencies.add("CHF");
    }

    @Override
    public Class<IReferenceCurrency> getContextInterface() { return IReferenceCurrency.class; }

    @Override
    public String key() { return KEY; }

    @Override
    protected IReferenceCurrency createInstance(String content) {
        return new ReferenceCurrency(content);
    }


    @Override
    public Map<String, String> getAvailableProperties() {

        StringBuilder b = new StringBuilder();

        b.append("ENUM(");
        for (String currency : currencies) {
            b.append(currency).append(',');
        }
        b.deleteCharAt(b.length() - 1);
        b.append(')');


        return Collections.singletonMap(key(), b.toString());
    }


    @Override
    protected String format(String content) {
        return content;
    }

    @Override
    protected String parse(String property) throws ContextValueTranslationException {

        if (!currencies.contains(property))
            throw new ContextValueTranslationException("Currency nor present in ENUM", key(), property, null);

        return property;
    }

    @Override
    protected String getContent(IReferenceCurrency instance) {
        return instance.getCurrency();
    }

}
