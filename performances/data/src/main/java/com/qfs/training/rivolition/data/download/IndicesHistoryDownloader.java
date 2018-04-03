package com.qfs.training.rivolition.data.download;


import java.util.HashSet;

/**
 * This class serves to download the indices history. This is in addition of the initial data.
 */
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
