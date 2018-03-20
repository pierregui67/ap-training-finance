package com.qfs.training.rivolition.data.main.download;

import com.qfs.training.rivolition.data.main.utilities.SerializableObject;

import java.util.HashSet;

public class StockPriceHistoryDownloader extends HistoryDownloader {


    public final static String FOLDER = "History/";
    protected final String PREFIX_URL = "https://finance.yahoo.com/quote/^";
    protected final String SUFFIX_URL = "/history?p=^";

    @Override
    public void main() {
        super.main();
        SerializableObject ser = new SerializableObject(symbolToDates);
        ser.serializableSaver(path + "symbolToDates.ser");
    }

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

