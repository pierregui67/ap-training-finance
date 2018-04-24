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
        final int prime = 31;
        int result = 1;
        long temp;

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        try {
            buffer.put(referenceCurrency.getBytes("UTF-8"));
            buffer.flip();
            temp = buffer.getLong();
            result = prime * result + (int) (temp ^ (temp >>> 32));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
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
