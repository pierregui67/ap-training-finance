package com.qfs.training.rivolition.data.main.download;

import com.qfs.training.rivolition.data.main.serializable.Indices;

import java.io.*;
import java.util.ArrayList;

public class IndicesHistoryDownloader extends HistoryDownloader {

    public final static String FOLDER = "IndicesHistory/";

    public void main() {
        this.getAll(super.indices);
    }

    public IndicesHistoryDownloader(String path) {
        super(path);

        File file = new File(path + "authorizedIndices.ser");
        // We try to open the file containing the serializable object
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            try {
                Indices authorizedIndices = (Indices) ois.readObject();
                indices = authorizedIndices.getAuthorizedIndices();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        // There is no serializable file, thus we try to generate it.
        } catch (IOException e) { // If there is no serializable file, then we try to generate it.
            new IndexDownloader(path).main(); // Files generation
            // Due to the new IndexDownloader there have been a reinitialisation
            init(path);
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                try {
                    Indices authorizedIndices = (Indices) ois.readObject();
                    indices = authorizedIndices.getAuthorizedIndices();
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
                // There still is no file despite the generation attempt.
            } catch (IOException e1) {
                System.out.println("ERROR. After trying to generate an Indices.ser file by " +
                        "calling an IndexDownloader, the files are still missing !");
                e1.printStackTrace();
            }
        }
    }


    protected String getFolder() {
        return FOLDER;
    }

}
