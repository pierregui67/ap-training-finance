package com.qfs.training.rivolition.data.download;

import com.qfs.training.rivolition.data.utilities.SerializableObject;

import java.util.HashSet;

/**
 * This class serves to download the stock symbol history.
 */
public class StockPriceHistoryDownloader extends HistoryDownloader {


    public final static String FOLDER = "History/";

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

