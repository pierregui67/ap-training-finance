package com.qfs.training.rivolition.data.main.download;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Class downloading historical data CSV of each portfolios symbol
 * @author Perseverance
 *
 */
public abstract class HistoryDownloader extends Downloader {

    protected static final String PREFIX_URL = "https://finance.yahoo.com/quote/^";
    protected static final String SUFFIX_URL = "/history?p=^";

    public HistoryDownloader(String path) {
        super(path);
    }

    @Override
    public String getInformation(List<Node> childNodes, int index) {
        try {
            return super.getInformation(childNodes.get(index).childNodes(),0);
        } catch (UnsupportedOperationException e) {
            return "";
        }
    }

    public void getAll(ArrayList<String> elements) {
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
        String fileName = "PriceHistory_" + replacedSym;

        // Connection to the URL
        Document doc = Jsoup.connect(url).timeout(10000000).get();

        // Only 100 elements. Why so few ?
        Elements elements = doc.getElementsByAttributeValueContaining("class", "BdT Bdc($c-fuji-grey-c) Ta(end) Fz(s) Whs(nw)");
        for (Element element : elements) {
            List<Node> childNodes = element.childNodes();

            dateString = getInformation(childNodes, indDate);
            try {
                date = dateFormat1.parse(dateString);
                dateString = dateFormat2.format(date).toString();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // When there is two nodes, that means the dividend is being display at this time.
            if (childNodes.size() == 2)
                record = record + dateString + ",,,,,," + System.getProperty("line.separator");
            else {

                open = getInformation(childNodes, indOpen).replace(",", "");
                close = getInformation(childNodes, indClose).replace(",", "");
                high = getInformation(childNodes, indHigh).replace(",", "");
                low = getInformation(childNodes, indLow).replace(",", "");
                adjClose = getInformation(childNodes, indAdjClose).replace(",", "");
                volume = getInformation(childNodes, indVolume).replace(",", "");

                // We record the data in a CSV format.
                record = record + dateString + "," + open + "," + high + "," + low + "," + close
                        + "," + volume + "," + adjClose + System.getProperty("line.separator");
            }
        }
        writter(record, fileName);

        System.out.println(" : written.");
    }

}
