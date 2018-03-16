package com.qfs.training.rivolition.data.main.download;

import org.jsoup.nodes.Node;

import java.io.*;
import java.util.ArrayList;
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

    protected void init(String path) {
        this.path = path;
        this.baseFolder = path + getFolder();
    }

    public Downloader(String path) {
        init(path);
        this.indices = new HashSet<String>();
        this.stockSymbols = new HashSet<String>();
    }

    public abstract void main();
    protected abstract String getFolder();
    protected abstract void parseURL(String sym) throws IOException;

    /*protected void serializableSaver(HashSet array, String fileName) {
        File file = new File(path + fileName);
        ObjectOutputStream oosSym = null;
        try {
            oosSym = new ObjectOutputStream(new FileOutputStream(file));
            SerializableArray ser = new SerializableArray(array);
            oosSym.writeObject(ser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

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
