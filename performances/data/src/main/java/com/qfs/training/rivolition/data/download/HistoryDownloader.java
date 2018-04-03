package com.qfs.training.rivolition.data.download;

import com.qfs.training.rivolition.data.utilities.Utils;
import com.qfs.training.rivolition.data.utilities.SerializableObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class downloading historical data CSV of each portfolios symbol
 */
public abstract class HistoryDownloader extends Downloader {

    protected final String PREFIX_URL = "https://finance.yahoo.com/quote/";
    protected final String SUFFIX_URL = "/history?p=";

    protected String fileNamePrefix="";

    protected static HashSet<String> target;

    @Override
    public void main() {
        this.getAll(target);
    }

    public HistoryDownloader(String path) {
        super(path);
    }

    protected String getDeeperInformation(List<Node> childNodes, int index) {
        try {
            return super.getInformation(childNodes.get(index).childNodes(),0);
        } catch (UnsupportedOperationException e) {
            return "";
        }
    }

    public void getAll(HashSet<String> elements) {
        for (String sym : elements) {
            try {
                parseURL(sym);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void parseURL(String sym) throws IOException {
        System.out.println(sym);

        String url;
        if (this.fileNamePrefix.equals("Index"))
            url = PREFIX_URL + "^" + sym + SUFFIX_URL + "^" + sym;
        else
            url = PREFIX_URL + sym + SUFFIX_URL + sym;

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
        String fileName = fileNamePrefix + "History_" + replacedSym;

        // Connection to the URL
        Document doc = Jsoup.connect(url).timeout(10000000).get();

        // Only 100 elements. Why so few ?
        Elements elements = doc.getElementsByAttributeValueContaining("class", "BdT Bdc($c-fuji-grey-c) Ta(end) Fz(s) Whs(nw)");
        for (Element element : elements) {
            List<Node> childNodes = element.childNodes();

            dateString = getDeeperInformation(childNodes, indDate);
            try {
                date = dateFormat1.parse(dateString);
                dateString = dateFormat2.format(date).toString();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (symbolToDates.containsKey(sym))
                symbolToDates.get(sym).add(dateString);
            else
                symbolToDates.put(sym, new HashSet<String>(Arrays.asList(dateString)));

            // When there is two nodes, that means the dividend is being display at this time.
            if (childNodes.size() == 2)
                record = record + dateString + ",,,,,," + System.getProperty("line.separator");
            else {

                open = getDeeperInformation(childNodes, indOpen).replace(",", "");
                close = getDeeperInformation(childNodes, indClose).replace(",", "");
                high = getDeeperInformation(childNodes, indHigh).replace(",", "");
                low = getDeeperInformation(childNodes, indLow).replace(",", "");
                adjClose = getDeeperInformation(childNodes, indAdjClose).replace(",", "");
                volume = getDeeperInformation(childNodes, indVolume).replace(",", "");

                // We record the data in a CSV format.
                record = record + dateString + "," + open + "," + high + "," + low + "," + close
                        + "," + volume + "," + adjClose + System.getProperty("line.separator");
            }
        }
        Utils.writter(record, baseFolder + fileName + FILE_EXTENSION);

        System.out.println(" : written.");
    }

    protected Object getSerializableObject(String serName) {

        // We try to open the file containing the utilities object
        try {
            return SerializableObject.readSerializable(this.path + serName);
            // There is no utilities file, thus we try to generate it.
        } catch (IOException e) { // If there is no utilities file, then we try to generate it.
            new IndexDownloader(this.path).main(); // Files generation
            // Due to the new IndexDownloader there have been a reinitialisation
            init(this.path);
            try {
                return SerializableObject.readSerializable(this.path + serName);
                // There still is no file despite the generation attempt.
            } catch (IOException e1) {
                System.out.println("ERROR. After trying to generate an Indices.ser file by " +
                        "calling an IndexDownloader, the files are still missing !");
                e1.printStackTrace();
            }
        }
        return null;
    }
}
