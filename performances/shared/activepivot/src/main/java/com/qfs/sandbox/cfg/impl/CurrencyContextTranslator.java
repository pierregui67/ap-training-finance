package com.qfs.sandbox.cfg.impl;

import com.quartetfs.biz.pivot.context.IContextValueTranslator;
import com.quartetfs.biz.pivot.context.impl.StringContextValueTranslator;
import com.quartetfs.fwk.QuartetPluginValue;

@QuartetPluginValue(intf = IContextValueTranslator.class)
public class CurrencyContextTranslator extends StringContextValueTranslator<ICurrencyContextValue> {
    private static final long serialVersionUID = -6005949179286212091L;
    public static final String KEY = "currency";

    @Override
    protected ICurrencyContextValue createInstance(String s) {
        return new CurrencyContextValue(s);
    }

    @Override
    protected String getContent(ICurrencyContextValue iCurrencyContextValue) {
        return iCurrencyContextValue.getCurrency();
    }

    @Override
    public Class<ICurrencyContextValue> getContextInterface() {
        return ICurrencyContextValue.class;
    }

    @Override
    public String key() {
        return KEY;
    }
}
