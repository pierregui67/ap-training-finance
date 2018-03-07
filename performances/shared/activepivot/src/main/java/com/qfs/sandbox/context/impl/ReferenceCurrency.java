package com.qfs.sandbox.context.impl;

import com.qfs.sandbox.context.IReferenceCurrency;
import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.context.impl.AContextValue;

final public class ReferenceCurrency extends AContextValue implements IReferenceCurrency {

    private final String currency;

    public ReferenceCurrency() {
        this.currency = "EUR";
    }

    public ReferenceCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String getCurrency() {
        return this.currency;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReferenceCurrency other = (ReferenceCurrency) obj;
        if (currency == null) {
            if (other.currency != null)
                return false;
        } else if (!currency.equals(other.currency))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public Class<? extends IContextValue> getContextInterface() {
        return IReferenceCurrency.class;
    }
}
