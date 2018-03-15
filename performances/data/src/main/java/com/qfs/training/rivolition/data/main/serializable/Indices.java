package com.qfs.training.rivolition.data.main.serializable;

import java.io.Serializable;
import java.util.ArrayList;

public class Indices implements Serializable {

    private ArrayList<String> authorizedIndices;

    public Indices(ArrayList<String> authorizedSymbols) {
        this.authorizedIndices = authorizedSymbols;
    }

    public ArrayList<String> getAuthorizedIndices() {
        return authorizedIndices;
    }

    public void setAuthorizedIndices(ArrayList<String> authorizedIndices) {
        this.authorizedIndices = authorizedIndices;
    }

}
