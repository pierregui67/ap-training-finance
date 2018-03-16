package com.qfs.training.rivolition.data.main.download;

import java.util.HashSet;

public class StockPriceHistoryDownloader extends HistoryDownloader {


    public final static String FOLDER = "History/";

    public StockPriceHistoryDownloader(String path) {
        super(path);
        this.fileNamePrefix = "Price";
        stockSymbols = (HashSet<String>) getSerializableObject("authorizedSymbols.ser");
        this.target = this.stockSymbols;
    }

    protected String getFolder() {
        return FOLDER;
    }
}

