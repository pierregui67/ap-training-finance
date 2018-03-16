package com.qfs.training.rivolition.data.main.download;


import java.util.HashSet;

public class IndicesHistoryDownloader extends HistoryDownloader {

    public final static String FOLDER = "IndicesHistory/";

    public IndicesHistoryDownloader(String path) {
        super(path);
        this.fileNamePrefix = "Index";
        this.indices = (HashSet<String>) getSerializableObject("authorizedIndices.ser");
        this.target = this.indices;
    }

    protected String getFolder() {
        return FOLDER;
    }

}
