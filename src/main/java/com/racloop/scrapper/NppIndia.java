package com.racloop.scrapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajanpunchouty on 30/05/17.
 */
public class NppIndia {

    public static final String PRICE_URL_PREFIX = "http://nppaindia.nic.in/nppaprice/newmedicinepricesearch.aspx";
    public static final String OUTPUT_FOLDER = "data";
    public static final String INPUT_FILE = OUTPUT_FOLDER + "/brands.csv";
    public static final String OUTPUT_FILE = OUTPUT_FOLDER + "/brand_details.csv";
    public static final String DELIMMITER = ",";

    public static void main(String[] args) {

    }

    private static List<String> parseInputFile() throws FileNotFoundException {
        List<String> records = new ArrayList<String>();
        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(INPUT_FILE));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] tokens = line.split(DELIMMITER);
                records.add(tokens[3]);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return records;
    }
}
