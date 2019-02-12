package com.qfs.sandbox.context.impl;

import com.qfs.sandbox.context.ICurrencyContextValue;
import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.context.impl.AContextValue;

import java.util.Objects;

public class CurrencyContextValue extends AContextValue implements ICurrencyContextValue {

    private String currency;

    public CurrencyContextValue(){}

    public CurrencyContextValue(String currency){
        this.currency = currency;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o) return true;
        // null check
        if (o == null) return false;
        // type check and cast
        if (getClass() != o.getClass()) return false;
        // field comparison
        CurrencyContextValue c = (CurrencyContextValue) o;
        return Objects.equals(currency, c.currency);
    }

    @Override
    public int hashCode() {
       return Objects.hash(currency);
    }

    @Override
    public Class<? extends IContextValue> getContextInterface() {
        return ICurrencyContextValue.class;
    }
}
