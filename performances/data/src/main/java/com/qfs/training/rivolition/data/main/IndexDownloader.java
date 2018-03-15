package com.qfs.training.rivolition.data.main;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


public class IndexDownloader {

    public static final String INDICES_LIST_URL = "https://finance.yahoo.com/world-indices";

    public static final String PREFIX_URL = "https://finance.yahoo.com/quote/";
    public static final String SUFFIX_URL = "/components?p=";

    public static String path;
    public static String baseFolder;
    public  final static String FOLDER = "Indices/";
    private final static String FILE_EXTENSION = ".csv";

    // All the reachable indices
    private final ArrayList<String> indices;

    // All the reachable stock symbol present in the different indices
    private final ArrayList<String> stockSymbols;

    public static ArrayList<String> main(String path) {

        try {
            IndexDownloader indexDownloader = new IndexDownloader(path);
            indexDownloader.parseURL(INDICES_LIST_URL);

            // Serialization of the stock symbols in order to get them whenever we want.
            File file = new File(path + "authorizedSymbols.ser");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            Symbols symbols = new Symbols(indexDownloader.getStockSymbols());
            oos.writeObject(symbols);

            return indexDownloader.stockSymbols;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public IndexDownloader(String path) {
        this.path = path;
        baseFolder = path + FOLDER;
        indices = new ArrayList<String>();
        stockSymbols = new ArrayList<String>();
    }

    public ArrayList<String> getStockSymbols() {
        return stockSymbols;
    }

    private void parseURL(String url) throws IOException {

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
                parseInterURL(indexName, PREFIX_URL + indexName + SUFFIX_URL + indexName);
            }
        }
    }

    private void parseInterURL(String indexName, String url) throws IOException {

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
        String record = "IndexName|Name Company|Last Price|Stock Symbol|Dump Number|Position Type|Date|Last Volume";

        File file = new File(baseFolder + indexNameClean + FILE_EXTENSION);
        FileWriter fr = null;
        BufferedWriter br = null;
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
            lastPrice = getInformation(childNodes, indLastPrice);
            volume = getInformation(childNodes, indVolume);

            // We load the date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dateFormat.format(date).toString();

            // We record the data in a CSV format.
            record = record + indexNameClean + "|" + nameCompany + "|" + lastPrice + "|" + stockSym
                    + "|42422424|" + "|equity|" + dateString + "|" + volume + System.getProperty("line.separator");

            stockSymbols.add(stockSym);
        }
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
        System.out.println(" : written.");
    }

    private String getInformation(List<Node> childNodes, int index) {
        try {
            return childNodes.get(index).childNodes().get(0).toString();
        }
        catch (IndexOutOfBoundsException e) {
            return "null";
        }
    }

}









