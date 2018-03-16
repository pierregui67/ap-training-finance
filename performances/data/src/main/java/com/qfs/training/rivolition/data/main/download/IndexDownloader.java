package com.qfs.training.rivolition.data.main.download;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.qfs.training.rivolition.data.main.serializable.SerializableObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


public class IndexDownloader extends Downloader {

    public static final String INDICES_LIST_URL = "https://finance.yahoo.com/world-indices";

    public static final String PREFIX_URL = "https://finance.yahoo.com/quote/";
    public static final String SUFFIX_URL = "/components?p=";

    public  final static String FOLDER = "Indices/";


    public void main() {

        try {
            this.getAll();

            // Serialization of the stock symbols in order to get them whenever we want.
            SerializableObject ser =  new SerializableObject<HashSet>(this.stockSymbols);
            ser.serializableSaver(this.path + "authorizedSymbols.ser");
            ser.setObj(this.indices);
            ser.serializableSaver(this.path+"authorizedIndices.ser");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public IndexDownloader(String path) {
        super(path);
    }

    protected String getFolder() {
        return FOLDER;
    }

    protected void getAll() throws IOException {

        String url = INDICES_LIST_URL;
        // Connection to the URL
        Document doc = Jsoup.connect(url).timeout(1000000).get();

        // Getting all the index of the Major World Indices. They are all characterized
        // by a "^" at the beginning of their name.
        // TODO : In fact that it false but it allow us to get most of them without any other noise.
        // TODO : Can we find a better criterion ?
        Elements elements = doc.getElementsByAttributeValueContaining("data-symbol","^");
        for (Element element : elements) {
            if(element.hasText() && element.hasAttr("title")) {

                // We get the index Name and then we move to the associated page giving the
                // information of its components.
                String indexName = element.attr("data-symbol");
                parseURL(indexName);
            }
        }
    }

    protected void parseURL(String indexName) throws IOException {
        String url = PREFIX_URL + indexName + SUFFIX_URL + indexName;

        // The needed data to be collected
        String nameCompany, stockSym, dateString, lastPrice, volume;
        Date date = new Date();

        // The node index of the wished information
        int indNameCompany = 1;
        int indStockSym = 0;
        int indLastPrice = 2;
        int indVolume = 5;

        // Connection to the URL
        Document doc = Jsoup.connect(url).timeout(1000000).get();

        String indexNameClean = indexName.replace("^", "");
        indices.add(indexNameClean);
        String record = "IndexName|Name Company|Last Price|Stock Symbol|Dump Number|" +
                "Position Type|Date|Last Volume" + System.getProperty("line.separator");


        System.out.println(indexNameClean);

        // Getting all the components of the index. They are the only elements of the page having
        // a dot in their title.
        Elements elements = doc.getElementsByAttributeValueContaining("title", ".");

        for (Element element : elements) {
            // We go to the node including all the information of the line.
            List<Node> childNodes = element.parent().parent().childNodes();

            // We retrieve the informations of the page.
            nameCompany = getInformation(childNodes, indNameCompany);
            stockSym = getInformation(childNodes.get(indStockSym).childNodes(), indStockSym);
            lastPrice = getInformation(childNodes, indLastPrice).replace(",","");
            volume = getInformation(childNodes, indVolume).replace(",","");

            // We load the date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dateFormat.format(date).toString();

            // We record the data in a CSV format.
            record = record + indexNameClean + "|" + nameCompany + "|" + lastPrice + "|" + stockSym
                    + "|42422424|equity|" + dateString + "|" + volume + System.getProperty("line.separator");

            stockSymbols.add(stockSym);
        }
        writter(record, indexNameClean);
        System.out.println(" : written.");
    }

}









