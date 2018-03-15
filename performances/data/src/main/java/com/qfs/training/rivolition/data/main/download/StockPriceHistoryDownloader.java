package com.qfs.training.rivolition.data.main.download;

import com.qfs.training.rivolition.data.main.serializable.Symbols;

import java.io.*;
import java.util.ArrayList;

public class StockPriceHistoryDownloader extends HistoryDownloader {


    public final static String FOLDER = "History/";

    public void main() {
        this.getAll(super.stockSymbols);
    }

    public StockPriceHistoryDownloader(String path) {
        super(path);

        File file = new File(path + "authorizedSymbols.ser");
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            try {
                Symbols authorizedSymbols = (Symbols) ois.readObject();
                this.stockSymbols = authorizedSymbols.getAuthorizedSymbols();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String getFolder() {
        return FOLDER;
    }
}

