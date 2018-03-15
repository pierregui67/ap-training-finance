package com.qfs.training.rivolition.data.main.download;

import org.jsoup.nodes.Node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Downloader {

    public static String path;
    public static String baseFolder;

    protected final static String FILE_EXTENSION = ".csv";

    // All the reachable indices
    protected ArrayList<String> indices;

    // All the reachable stock symbol present in the different indices
    protected ArrayList<String> stockSymbols;

    protected void init(String path) {
        this.path = path;
        this.baseFolder = path + getFolder();
    }

    public Downloader(String path) {
        init(path);
        this.indices = new ArrayList<String>();
        this.stockSymbols = new ArrayList<String>();
    }

    public abstract void main();
    protected abstract String getFolder();
    protected abstract void parseURL(String sym) throws IOException;

    protected void writter(String record, String fileName) {
        File file = new File(baseFolder + fileName + FILE_EXTENSION);
        FileWriter fr = null;
        BufferedWriter br = null;

        try {
            fr = new FileWriter(file);
            br = new BufferedWriter(fr);
            br.write(record);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected String getInformation(List<Node> childNodes, int index) {
        try {
            return childNodes.get(index).childNodes().get(0).toString();
        }
        catch (IndexOutOfBoundsException e) {
            return "";
        }
    }
}
