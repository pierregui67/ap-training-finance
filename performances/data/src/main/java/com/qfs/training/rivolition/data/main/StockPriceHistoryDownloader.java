package com.qfs.training.rivolition.data.main;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


public class StockPriceHistoryDownloader {

    private static final String PREFIX_URL = "https://finance.yahoo.com/quote/";
    private static final String SUFFIX_URL = "/history?p=";
    ;

    public static String path;
    public static String baseFolder;
    public final static String FOLDER = "History/";
    private final static String FILE_EXTENSION = ".csv";

    // All the reachable indices
    private ArrayList<String> indices;

    // All the reachable stock symbol present in the different indices
    private ArrayList<String> stockSymbols;


    public static void main(String path, ArrayList<String> authorizedSymbols) {
        StockPriceHistoryDownloader sphDownloader;
        if (authorizedSymbols.isEmpty())
            sphDownloader = new StockPriceHistoryDownloader(path);
        else
            sphDownloader = new StockPriceHistoryDownloader(path, authorizedSymbols);
        sphDownloader.getAllSymbols();
    }

    private void init(String path) {
        this.path = path;
        baseFolder = path + FOLDER;
        this.indices = new ArrayList<String>();
    }

    public StockPriceHistoryDownloader() {
    }

    public StockPriceHistoryDownloader(String path, ArrayList<String> authorizedSymbols) {
        this.init(path);
        stockSymbols = authorizedSymbols;
    }

    public StockPriceHistoryDownloader(String path) {
        this.init(path);

        File file = new File(path + "authorizedSymbols.ser");
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            try {
                Symbols authorizedSymbols = (Symbols) ois.readObject();
                stockSymbols = authorizedSymbols.getAuthorizedSymbols();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getAllSymbols() {
        for (String sym : stockSymbols) {
            try {
                parseURL(sym);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseURL(String sym) throws IOException {

        String url = PREFIX_URL + sym + SUFFIX_URL + sym;

        // The needed data to be collected
        String open, high, low, close, volume, adjClose, dateString;
        Date date;
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");

        String record="Date,Open,High,Low,Close,Volume,Adj Close" + System.getProperty("line.separator");

        // The node index of the wished information
        int indDate = 0;
        int indOpen = 1;
        int indHigh = 2;
        int indLow = 3;
        int indClose = 4;
        int indAdjClose = 5;
        int indVolume = 6;

        String replacedSym = sym.replace(".", "-");
        File file = new File(baseFolder + "PriceHistory_" + replacedSym + FILE_EXTENSION);
        FileWriter fr = null;
        BufferedWriter br = null;
        System.out.println(sym);

        // Connection to the URL
        Document doc = Jsoup.connect(url).timeout(10000000).get();

        // Only 100 elements. Why so few ?
        Elements elements = doc.getElementsByAttributeValueContaining("class", "BdT Bdc($c-fuji-grey-c) Ta(end) Fz(s) Whs(nw)");
        for (Element element : elements) {

            List<Node> childNodes = element.childNodes();

            dateString = getInformation(childNodes, indDate);
            //dateString = dateString.replace(" ","-");
            //dateString = dateString.replace(",","");
            try {
                date = dateFormat1.parse(dateString);
                dateString = dateFormat2.format(date).toString();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            open = getInformation(childNodes, indOpen).replace(",","");
            close = getInformation(childNodes, indClose).replace(",","");
            high = getInformation(childNodes, indHigh).replace(",","");
            low = getInformation(childNodes, indLow).replace(",","");
            adjClose = getInformation(childNodes, indAdjClose).replace(",","");
            volume = getInformation(childNodes, indVolume).replace(",","");

            // We record the data in a CSV format.
            record = record + dateString + "," + open + "," + high + "," + low + "," + close
                + "," + volume + "," + adjClose + System.getProperty("line.separator");
        }
        try {
            fr = new FileWriter(file);
            br = new BufferedWriter(fr);
            br.write(record);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(" : written.");
    }

    private String getInformation(List<Node> childNodes, int index) {
        try {
            return childNodes.get(index).childNodes().get(0).childNodes().get(0).toString();
        } catch (IndexOutOfBoundsException e) {
            return "null";
        } catch (UnsupportedOperationException e) {
            return "null";
        }
    }

}

