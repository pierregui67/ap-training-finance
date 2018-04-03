package com.qfs.training.rivolition.data.download;

import com.qfs.training.rivolition.data.utilities.SerializableObject;
import com.qfs.training.rivolition.data.utilities.Utils;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class serves to download the information of the stock symbols.
 */
public class SectorDownloader extends HistoryDownloader {

    public static final String PREFIX_URL = "https://finance.yahoo.com/quote/";
    public static final String SUFFIX_URL = "/profile?p=";

    public  final static String FOLDER = "Sectors/";

    HashSet<String> industries;

    /**
     * Map the industries to their sector.
     * Key : the industry.
     * Value : the sector it belong.
     */
    HashMap<String, String> industryBelongToSector;

    /**
     * Map the industries to their file contend.
     * Key : the industry
     * Value : the record to write in the file associated to the industry.
      */
    HashMap<String, String> records;

    private final int IND_SECTOR = 4;
    private final int IND_INDUSTRY = 10;

    public SectorDownloader(String path) {
        super(path);
        this.stockSymbols = (HashSet<String>) getSerializableObject("authorizedSymbols.ser");
        this.industries = new HashSet<String>();
        this.records = new HashMap<String, String>();
        this.industryBelongToSector = new HashMap<String, String>();
    }

    public void main() {
        this.getAll(this.stockSymbols);
        System.out.println(">>> Serialization");
        SerializableObject ser = new SerializableObject<HashMap>(this.records);
        ser.serializableSaver(this.path+"records.ser");
        ser.setObj(this.industryBelongToSector);
        ser.serializableSaver(this.path+"industryToSector.ser");
        ser = new SerializableObject<HashSet>(industries);
        ser.serializableSaver(this.path + "industries.ser");

        // Now saving the records.
        System.out.println(">>> Recording");
        for (String industry : industries) {
            String record = records.get(industry);
            String sector = industryBelongToSector.get(industry);
            baseFolder += sector + "/";

            // We check the directory exists. If not, we create it.
            File directory = new File(baseFolder);
            if (! directory.exists()) {
                directory.mkdir();
            }

            Utils.writter(record, baseFolder + industry + FILE_EXTENSION);
            // If we do not rectified the baseFolder, each new folder will be created inside
            // the previous one.
            baseFolder = baseFolder.replace(sector,"");

        }
    }

    protected String getFolder() {
        return FOLDER;
    }

    @Override
    protected String getInformation(List<Node> childNodes, int index) {
        try {
            return childNodes.get(index).childNodes().get(0).toString();
        }
        catch (IndexOutOfBoundsException e) {
            // there is a blank on Yahoo! Finance. For instance CCH.L
            return "Unknown";
        }
    }

    @Override
    protected void parseURL(String sym) throws IOException {
        String sector="", industry="", fullName="";

        int numberOfChild = 17;

        System.out.println(sym);
        String url = PREFIX_URL + sym + SUFFIX_URL + sym;

        Document doc;
        Elements elements;
        try {
            doc = Jsoup.connect(url).timeout(1000000000).get();
            elements = doc.getElementsByAttributeValueContaining("class","D(ib) Va(t)");
        }
        catch (UncheckedIOException e) {
            System.out.println("Exception in thread main org.jsoup.UncheckedIOException: " +
                    "java.util.zip.ZipException: Corrupt GZIP trailer");
            System.out.println("Stock Symbols : " + sym);
            System.out.println("Continuing ...");
            return;
        }

        // Usually there is three objects in elements. The element of interest is the third one which
        // has 17 childs.
        int checker = 0;
        for (Element e : elements) {
            if (e.childNodes().size() == numberOfChild) {
                checker ++;
                sector = getInformation(e.childNodes(),IND_SECTOR).replaceAll("/", " - ");
                industry = getInformation(e.childNodes(), IND_INDUSTRY).replaceAll("/"," - ");

                if (! industries.contains(industry)) {
                    industries.add(industry);
                    industryBelongToSector.put(industry, sector);
                    records.put(industry, "Stock Symbol|Name Company|Sector|Industry" + System.getProperty("line.separator"));
                }

            }
        }
        // We check that there is one and only one element verifying the above properties.
        if (checker != 1) {
            System.out.println("ERROR : too much elements verifying the properties. We can not" +
                    " discriminate which element contains the right informations. To fix this issue, " +
                    "add rules.");
            return;
        }
        // Should contain a single element containing the full name of the company.
        elements = doc.getElementsByAttributeValueContaining("class","Fz(m) Mb(10px)");
        if (elements.size() != 1) {
            System.out.println("ERROR : too much elements verifying the properties when trying " +
                    "to get the full name of the company");
            return;
        }
        fullName = elements.get(0).childNode(0).toString();
        records.put(industry, records.get(industry) + sym + "|" + fullName + "|" + sector + "|" + industry
                + System.getProperty("line.separator"));
        System.out.println(" : recorded");
    }
}
