package com.racloop.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajanpunchouty on 27/05/17.
 */
public class Scrapper {

    public static final String DRUG_INFO_URL_PREFIX = "http://www.medindia.net/doctors/drug_information";
    public static final String TRADE_NAME_URL_PREFIX = "http://www.medindia.net/drugs/trade-names";
    public static final String OUTPUT_FOLDER = "data";
    public static final String DELIMMITER = ",";
    public static MessageDigest md;


    public static void main(String[] args) throws NoSuchAlgorithmException {

        md = MessageDigest.getInstance("MD5");
        HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setJavascriptEnabled(false);
        List<String[]> records = runForAllAlphabets(driver);
        writeToFile(OUTPUT_FOLDER + "/generics.csv", records);
        List<String[]> brands = getDetails(driver, records);
        writeToFile(OUTPUT_FOLDER + "/brands.csv", brands);
        driver.quit();
    }

    private static List<String[]> getDetails(HtmlUnitDriver driver, List<String[]> records) {
        List<String[]> brands = new ArrayList<String[]>();
        for (String[] record : records) {
            String genericLinkSuffix = record[2];
            String url = TRADE_NAME_URL_PREFIX + genericLinkSuffix;
            System.out.println("Processing Brand : " + url);
            driver.get(url);
            try {
                WebElement table = driver.findElement(By.className("report-content"));

                List<WebElement> rows = table.findElements(By.tagName("tr"));
                for (WebElement row : rows) {
                    String style = row.getAttribute("style");
                    if(style == null) {
                        List<WebElement> anchors = row.findElements(By.tagName("a"));
                        for (WebElement anchor : anchors) {
                            String name = anchor.getText();
                            String link = anchor.getAttribute("href");
//                        String md5Hash = getMd5Hash(name);
                            brands.add(new String[]{record[3], genericLinkSuffix, name, link});
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return brands;
    }

    private static List<String[]> runForAllAlphabets(WebDriver driver) {
        List<String[]> records = new ArrayList<String[]>();
        for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {
            records.addAll(scrapForSingleAlphabet(alphabet, driver, records.size()));
        }
        return records;
    }

    private static List<String[]> scrapForSingleAlphabet(char alphabet, WebDriver driver, int index) {
        String baseUrl = DRUG_INFO_URL_PREFIX + "/home.asp?alpha=" + alphabet;
        System.out.println("Processing Generics: " + baseUrl);
        driver.get(baseUrl);
        List<WebElement> elements = driver.findElements(By.className("list-item"));
        List<String[]> records = new ArrayList<String[]>();
        for (WebElement element : elements) {
            WebElement aElement = element.findElement(By.tagName("a"));
            String name = aElement.getText();
            String link = aElement.getAttribute("href");
            String suffix = link.substring(link.indexOf("drug_information") + "drug_information".length());
            String text = element.getText();
            String description = text.substring(text.indexOf("\n") + 1);
//            String md5Hash = getMd5Hash(name);
            records.add(new String[]{index+"", link, suffix, name});
//            records.add(new String[]{"\"" + ++index + "\"", "\"" + md5Hash + "\"", "\"" + link + "\"", "\"" + suffix + "\"", "\"" + name + "\""});
        }
        return records;
    }

    private static void writeToFile(String fileName, List<String[]> records) {
        FileWriter writer = null;
        File file = new File(fileName);
        try {
            writer = new FileWriter(file, true);
            for (String[] record : records) {
                for (String s : record) {
                    writer.append(s + DELIMMITER);
                }
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getMd5Hash(String input) {
        md.reset();
        md.update(input.getBytes());
        byte byteData[] = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
