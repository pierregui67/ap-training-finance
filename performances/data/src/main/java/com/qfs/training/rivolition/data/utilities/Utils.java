package com.qfs.training.rivolition.data.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

    /**
     * Write the record in a file.
     * The fileName must be composed of : baseFolder + fileName + FILE_EXTENSION
     */
    public static void writter(String record, String fileName) {
        File file = new File(fileName);
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
}
