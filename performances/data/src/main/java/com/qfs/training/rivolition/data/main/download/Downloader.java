package com.qfs.training.rivolition.data.main.download;

import org.jsoup.nodes.Node;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public abstract class Downloader {

    public static String path;
    public static String baseFolder;

    protected final static String FILE_EXTENSION = ".csv";

    // All the reachable indices
    protected HashSet<String> indices;

    // All the reachable stock symbol present in the different indices
    protected HashSet<String> stockSymbols;

    protected HashMap<String, HashSet<String>> indexToSymbols;

    protected HashMap<String, HashSet<String>> symbolToDates;

    protected void init(String path) {
        this.path = path;
        this.baseFolder = path + getFolder();
    }

    public Downloader(String path) {
        init(path);
        this.indices = new HashSet<String>();
        this.stockSymbols = new HashSet<String>();
        this.indexToSymbols = new HashMap<String, HashSet<String>>();
        this.symbolToDates = new HashMap<String, HashSet<String>>();
    }

    public abstract void main();
    protected abstract String getFolder();
    protected abstract void parseURL(String sym) throws IOException;

    protected String getInformation(List<Node> childNodes, int index) {
        try {
            return childNodes.get(index).childNodes().get(0).toString();
        }
        catch (IndexOutOfBoundsException e) {
            return "";
        }
    }
}
