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
import java.util.zip.ZipException;

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

    protected String getFileName(String sym) {
        String replacedSym = sym.replace(".", "-");
        String fileName = baseFolder + fileNamePrefix + "History_" + replacedSym + FILE_EXTENSION;
        return fileName;
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



        // The node index of the wished information
        int indDate = 0;
        int indOpen = 1;
        int indHigh = 2;
        int indLow = 3;
        int indClose = 4;
        int indAdjClose = 5;
        int indVolume = 6;

        //String replacedSym = sym.replace(".", "-");
        //String fileName = baseFolder + fileNamePrefix + "History_" + replacedSym + FILE_EXTENSION;
        String fileName = getFileName(sym);

        File f = new File(fileName);
        boolean fileExist = f.exists() && !f.isDirectory();
        String record = fileExist ? "" : "Date,Open,High,Low,Close,Volume,Adj Close" + System.getProperty("line.separator");

        // Connection to the URL
        try {
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

                if (symbolToDates.containsKey(sym)) {
                    // If the date is already recorded
                    if (symbolToDates.get(sym).contains(dateString))
                        continue;
                    symbolToDates.get(sym).add(dateString);
                }
                else
                    symbolToDates.put(sym, new HashSet<String>(Arrays.asList(dateString)));


                // When there is two nodes, that means the dividend is being display at this time.
                if (childNodes.size() == 2)
                    record = record;// + dateString + ",,,,,," + System.getProperty("line.separator");
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
            FileWriter fw = new FileWriter(fileName, fileExist);
            fw.write(record);
            fw.close();
            /*if (!fileExist)
                Utils.writter(record, fileName);
            else {

            }*/

            System.out.println(" : written.");
        } catch (UncheckedIOException e) {
            System.out.println("UncheckedIOException");
        } catch (ZipException z) {
            System.out.println("ZipException");
        }

    }

    protected Object getSerializableObject(String serName) {

        // We try to open the file containing the utilities object
        try {
            return SerializableObject.readSerializable(this.path + serName);
            // There is no utilities file, thus we try to generate it.
        } catch (IOException e) { // If there is no utilities file, then we try to generate it.
            if (serName.equals("symbolToDates.ser"))
                return new HashMap<String, HashSet<String>>();
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

    public void correctData() {

        Scanner scanner = null;
        for (String sym : this.stockSymbols) {
            String record = "";
            try {
                scanner = new Scanner(new File(getFileName(sym)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains(",,,,,,")) {
                    String date = line.substring(0,10);
                    this.symbolToDates.get(sym).remove(date);
                }
                else
                    record = record + line + System.getProperty("line.separator");
            }
            FileWriter fw = null;
            try {
                fw = new FileWriter(getFileName(sym));
                fw.write(record);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
