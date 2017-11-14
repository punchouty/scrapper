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
 * Created by rajanpunchouty on 13/06/17.
 */
public class NetMeds {

    public static final String OUTPUT_FOLDER = "data";
    public static final String OUTPUT_FILE = OUTPUT_FOLDER + "/med.csv";
    public static final String COMPANY_URL = "http://www.netmeds.com/medicine/manufacturers";

    public static final String DELIMMITER = "~";
    public static final String LINE_SATRT = "`";
    public static final String SEPERATOR = "^";


    public static void main(String[] args) {
        List<String> companiesUrls = new ArrayList<String>();
        HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setJavascriptEnabled(false);
        driver.get(COMPANY_URL);
        WebElement fourCol = driver.findElement(By.className("four-col"));
        List<WebElement> links = fourCol.findElements(By.tagName("a"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            companiesUrls.add(href);
        }
        int index = 0;
        for (String companiesUrl : companiesUrls) {
            if(index <= 633) {
                System.out.println("Skipping index : " + index++ + " | " + companiesUrl);
            }
            else {
                System.out.println("Process company : " + companiesUrl);
                scrapForSingleCompany(companiesUrl, driver, index++);

            }
        }

    }

    private static void scrapForSingleCompany(String companyUrl, WebDriver driver, int index) {
        List<String> webLinks = new ArrayList<String>();
        System.out.println("Processing Company Ur : " + companyUrl);
        driver.get(companyUrl);
        List<WebElement> alphabets = driver.findElements(By.className("drug-list-col"));
        for (WebElement alphabet : alphabets) {
            List<WebElement> lis = alphabet.findElements(By.className("panel"));
            for (WebElement li : lis) {
                WebElement panelBody = li.findElement(By.className("panel-body"));
                List<WebElement> links = panelBody.findElements(By.tagName("a"));
                for (WebElement link : links) {
                    String name = link.getText();
                    String href = link.getAttribute("href");
                    webLinks.add(href);
                }
                
            }
        }
        List<String[]> records = new ArrayList<String[]>();
        for (String webLink : webLinks) {
            System.out.println(webLink);
            String[] values = scrapForSinglePage(webLink, driver);
            if(values != null) records.add(values);
        }
        writeToFile(records, index);
    }

    private static String[] scrapForSinglePage(String url, WebDriver driver) {
        try {
            System.out.println("Processing brand : " + url);
            driver.get(url);
            WebElement brandName = driver.findElement(By.id("topBrandname"));
            String name = brandName.getText();
            WebElement img = driver.findElement(By.id("topBImg"));
            String srcLink = img.getAttribute("src");
            String type = srcLink.substring(srcLink.lastIndexOf('/') + 1);
            type = type.substring(0, type.indexOf('.'));
            WebElement genericSpan = driver.findElement(By.id("topBGeneric"));
            String genericName = genericSpan.getText();
            WebElement priceSpan = driver.findElement(By.id("lblBmrp"));
            String price = priceSpan.getText().substring(priceSpan.getText().indexOf('.') + 1);

            WebElement manufactureLink = driver.findElement(By.id("BManu"));
            String manufacturer = manufactureLink.getText();
            System.out.println("Values : " + name + "|" + type + "|" + genericName + "|" + price + "|" + manufacturer);
            return new String[]{"\"" + name + "\"", "\"" + type + "\"", "\"" + genericName + "\"", "\"" + price + "\"", "\"" + manufacturer + "\""};
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void writeToFile(List<String[]> records, int index) {
        FileWriter writer = null;
        File file = new File("data" + "/" + index + ".csv");
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
}
