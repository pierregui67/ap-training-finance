package com.qfs.training.rivolition.data.main;

import java.io.Serializable;
import java.util.ArrayList;

public class Symbols implements Serializable {

    private ArrayList<String> authorizedSymbols;

    public Symbols(ArrayList<String> authorizedSymbols) {
        this.authorizedSymbols = authorizedSymbols;
    }

    public ArrayList<String> getAuthorizedSymbols() {
        return authorizedSymbols;
    }

    public void setAuthorizedSymbols(ArrayList<String> authorizedSymbols) {
        this.authorizedSymbols = authorizedSymbols;
    }

}
