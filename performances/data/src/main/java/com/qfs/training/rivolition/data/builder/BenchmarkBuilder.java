package com.qfs.training.rivolition.data.builder;

import com.qfs.training.rivolition.data.utilities.SerializableObject;
import com.qfs.training.rivolition.data.utilities.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 This class serve to build benchmark portfolios.
 There are constituted by 100 shares for each stock symbols of the index.
 */
public class BenchmarkBuilder {

    public static String path;
    public static final String BASE_FOLDER = "activepivot-server/src/main/resources/data/";

    protected final static String FILE_EXTENSION = ".csv";

    // All the reachable indices
    protected static HashSet<String> indices;


    protected static HashMap<String, HashSet<String>> indexToSymbols;
    protected static HashMap<String, HashSet<String>> symbolToDates;

    public static void main(String[] args) {
        loadData();
        for (String index : indices) {

            String record = "";
            HashSet<String> stockSymbols = new HashSet<String>();
            if (indexToSymbols.containsKey(index))
                stockSymbols = indexToSymbols.get(index);
            for (String sym : stockSymbols) {

                HashSet<String> dates = new HashSet<String>();
                if (symbolToDates.containsKey(sym))
                     dates = symbolToDates.get(sym);
                for (String date : dates) {
                    record = record + date + "|" + index + "_benchmark|100|" + sym + "|Regular|"
                            + System.getProperty("line.separator");
                }
            }
            Utils.writter(record, BASE_FOLDER + "Portfolios/Initial/" + index + "_benchmark"
                    + FILE_EXTENSION);
        }
    }

    private static void loadData() {
        try {
            indices = (HashSet<String>) SerializableObject.readSerializable(BASE_FOLDER + "authorizedIndices.ser");
            indexToSymbols = (HashMap<String, HashSet<String>>) SerializableObject.readSerializable(BASE_FOLDER + "indexToSymbols.ser");
            symbolToDates = (HashMap<String, HashSet<String>>) SerializableObject.readSerializable(BASE_FOLDER + "symbolToDates.ser");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
