package com.racloop.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajanpunchouty on 24/05/17.
 */
public class Main {

    public static final String OUTPUT_FOLDER = "data";
    public static final String INPUT_FILE = OUTPUT_FOLDER + "/brands.csv";
    public static final String OUTPUT_FILE = OUTPUT_FOLDER + "/brand_details.csv";
    public static final String PRICE_URL_PREFIX = "http://www.medindia.net/drug-price";
    public static final String DELIMMITER = ",";

    public static void main(String [] args) throws FileNotFoundException {
        HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setJavascriptEnabled(false);
        List<String> urls = parseInputFile();
//        List<String> urls = new ArrayList<String>();
//        urls.add("http://www.medindia.net/drug-price/cefpodoxmine-proxetil-combination/acef-lb-50pluslb.htm");
        for (String url : urls) {
            String[] records = processBrandUrl(url, driver);
            if(records != null) writeToFile(OUTPUT_FILE, records);
        }

    }

    private static String[] processBrandUrl(String url, HtmlUnitDriver driver) {
        System.out.println("Processing url : " + url);
        driver.get(url);
        List<WebElement> tables = driver.findElements(By.className("table"));
        for (WebElement table : tables) {
            WebElement h3 = driver.findElement(By.tagName("h3"));
            if(h3.getText().contains("Price Details")) {

                WebElement priceTable = tables.get(1);
                List<WebElement> rows = priceTable.findElements(By.tagName("tr"));

                List<WebElement> tradeNameCells = rows.get(1).findElements(By.tagName("td"));
                String tradeName = tradeNameCells.get(1).getText();

                List<WebElement> manufacturerCells = rows.get(2).findElements(By.tagName("td"));
                String manufacturer = manufacturerCells.get(1).getText();

                List<WebElement> unitCells = rows.get(3).findElements(By.tagName("td"));
                String units = unitCells.get(1).getText();

                List<WebElement> typeCells = rows.get(4).findElements(By.tagName("td"));
                String type = typeCells.get(1).getText();

                List<WebElement> quantityCells = rows.get(5).findElements(By.tagName("td"));
                String quantity = quantityCells.get(1).getText();

                List<WebElement> priceCells = rows.get(6).findElements(By.tagName("td"));
                String price = priceCells.get(1).getText();

                WebElement h1 = driver.findElement(By.tagName("h1"));
                String h1Text = h1.getText();

                String genericString = h1Text.substring(tradeName.length(), h1Text.indexOf("Price List")).trim();
                String genericNames = genericString.substring(1, genericString.length() -1);
                String concatGenericNames = genericNames.replace(", ", "|");

                return new String[]{tradeName, manufacturer, units, type, quantity, price, genericNames, concatGenericNames};
            }
        }
        return null;
    }

    private static void writeToFile(String fileName, String[] record) {
        FileWriter writer = null;
        File file = new File(fileName);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(file, true);
            for (String s : record) {
                writer.append(s + DELIMMITER);
            }
            writer.append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
