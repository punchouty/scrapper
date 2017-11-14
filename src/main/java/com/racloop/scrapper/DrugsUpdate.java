package com.racloop.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajanpunchouty on 31/05/17.
 */
public class DrugsUpdate {

    public static final String OUTPUT_FOLDER = "data";
    public static final String OUTPUT_FILE = OUTPUT_FOLDER + "/brand_details.csv";
    public static final String PRICE_URL_PREFIX = "http://www.drugsupdate.com/brand/filter";
    public static final String DELIMMITER = "~";
    public static final String LINE_SATRT = "`";
    public static final String SEPERATOR = "^";

    public static void main(String[] args) {
        HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setJavascriptEnabled(false);
        runForAllAlphabets(driver);
    }

    private static void runForAllAlphabets(WebDriver driver) {
        for (char alphabet = 'b'; alphabet <= 'z'; alphabet++) {
            scrapForSingleAlphabet(alphabet, driver);
        }
//        scrapForSingleAlphabet('a', driver);
    }

    private static void scrapForSingleAlphabet(char alphabet, WebDriver driver) {
        String baseUrl = PRICE_URL_PREFIX + "/" + alphabet;
        List<String[]> records = new ArrayList<String[]>();
        driver.get(baseUrl);
        int pageCount = getPageCount(driver);
        System.out.println("Page Count: " + pageCount);
        for (int i = 1; i <= pageCount; i++) {
            String pageUrl =  baseUrl + "/" + i;
            System.out.println("Processing page: " + pageUrl);
            driver.get(pageUrl);
            List<WebElement> tables = driver.findElements(By.tagName("table"));
            for (WebElement table : tables) {
                String cellSpacing = table.getAttribute("cellspacing");
                if (cellSpacing != null && cellSpacing.equals("5")) {
                    System.out.println("Got s successful page : " + i);
                    List<WebElement> brands = table.findElements(By.className("brand_row"));
                    for (WebElement brand : brands) {
                        WebElement titleInfo = brand.findElement(By.className("title"));
                        List<WebElement> anchors = titleInfo.findElements(By.tagName("a"));
                        String name = anchors.get(0).getText();
                        String company = anchors.get(1).getText();
                        String generics = anchors.get(2).getText();

                        List<WebElement> rows = brand.findElements(By.className("strengthlisting"));
                        WebElement header = rows.get(0);
                        List<WebElement> headerCells = header.findElements(By.tagName("td"));
                        boolean isCombinitation = headerCells.get(0).getText().equals("Combination");
                        rows.remove(0);
                        for (WebElement row : rows) {
                            List<WebElement> cells = row.findElements(By.tagName("td"));
                            String text = cells.get(0).getText();
                            String volume = cells.get(1).getText();
                            String presentation = cells.get(2).getText();
                            String price = cells.get(3).getText();
                            String[] record = {LINE_SATRT, name, company, generics, isCombinitation+"", text.replace("\n", SEPERATOR), volume, presentation, price};
                            records.add(record);
                        }
                    }
                }
            }
        }
        writeToFile(OUTPUT_FOLDER + "/" + alphabet + ".txt", records);

    }

    private static int getPageCount(WebDriver driver) {
        int pageCount = 0;
        WebElement pagination = driver.findElement(By.className("paging"));
        String paginationText = pagination.getText();
        String numberOfRecordsStr = paginationText.substring(paginationText.indexOf("total") + "total".length(), paginationText.indexOf("records"));
        int numberOfRecords = Integer.valueOf(numberOfRecordsStr.trim());
        if(numberOfRecords % 20 == 0) pageCount = numberOfRecords/20;
        else pageCount = numberOfRecords/20 + 1;
        System.out.println(pageCount + " | " + numberOfRecords);
        return pageCount;
    }

    private static void writeToFile(String fileName, List<String[]> records) {
        FileWriter writer = null;
        File file = new File(fileName);
        try {
            writer = new FileWriter(file, false);
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

    private static List<String[]> scrapForSinglePage(char alphabet, WebDriver driver, int index) {
        String baseUrl = PRICE_URL_PREFIX + "/home.asp?alpha=" + alphabet;
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
            records.add(new String[]{index + "", link, suffix, name});
//            records.add(new String[]{"\"" + ++index + "\"", "\"" + md5Hash + "\"", "\"" + link + "\"", "\"" + suffix + "\"", "\"" + name + "\""});
        }
        return records;
    }
}
