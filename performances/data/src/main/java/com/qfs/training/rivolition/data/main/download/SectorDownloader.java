package com.qfs.training.rivolition.data.main.download;

import java.io.IOException;
import java.util.ArrayList;

public class SectorDownloader extends Downloader {

    public static final String PREFIX_URL = "https://finance.yahoo.com/quote/";
    public static final String SUFFIX_URL = "/profile?p=";

    public  final static String FOLDER = "Sectors/";

    public SectorDownloader(String path, ArrayList<String> authorizedSymbols) {
        super(path);
    }

    public void main() {

    }

    protected String getFolder() {
        return FOLDER;
    }

    protected void parseURL(String sym) throws IOException {

    }
}
