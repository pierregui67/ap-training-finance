package com.qfs.sandbox.context.impl;

import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.context.impl.AContextValue;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ReferenceCurrency extends AContextValue implements IReferenceCurrency {

    private static final long serialVersionUID = 201804231519L;
    private String referenceCurrency;

    public ReferenceCurrency(){
        this.referenceCurrency = "EUR";
    }

    public ReferenceCurrency(String referenceCurrency){
        this.referenceCurrency = referenceCurrency;
    }

    @Override
    public String getReferenceCurrency() {
        return referenceCurrency;
    }

    public void setReferenceCurrency(String referenceCurrency) {
        this.referenceCurrency = referenceCurrency;
    }

    @Override
    public int hashCode() {

        int hash = 7;
        for (int i = 0; i < referenceCurrency.length(); i++) {
            hash = hash*31 + referenceCurrency.charAt(i);
        }

        return hash;
    }

    @Override
    public Class<? extends IContextValue> getContextInterface() {
        return IReferenceCurrency.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) //compare reference
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;


        if (!this.referenceCurrency.equals(((ReferenceCurrency)o).getReferenceCurrency())){
            return false;
        }

        return true;
    }
}
