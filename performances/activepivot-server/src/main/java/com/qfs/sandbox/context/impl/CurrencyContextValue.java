package com.qfs.sandbox.context.impl;

import com.qfs.sandbox.context.ICurrencyContextValue;
import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.context.impl.AContextValue;

final public class CurrencyContextValue extends AContextValue implements ICurrencyContextValue {

    /* Can not be final. Must be setted during the initialization process */
    private String currency;

    public CurrencyContextValue() {
    }

    public CurrencyContextValue(String currency) {
        this.currency = currency;
    }

    @Override
    public String getCurrency() {
        return this.currency;
    }

    /* Needed by jaxb */
    private void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CurrencyContextValue other = (CurrencyContextValue) obj;
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
        return ICurrencyContextValue.class;
    }


}
