package com.racloop.scrapper;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Created by rajanpunchouty on 16/06/17.
 */
public class FileStitch {

    public static final String OBJECT_FILE_NAME = "output/products.obj";
    public static final String CSV_FILE_NAME = "output/products.csv";

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        File objectFile = new File(OBJECT_FILE_NAME);
        if(objectFile.exists()) {
            objectFile.delete();
        }

        File csvFile = new File(CSV_FILE_NAME);
        if(csvFile.exists()) {
            csvFile.delete();
        }

//        ArrayList<ProductTmp> names = readProductsFromCsv();
//        HashMap<String, Integer> map = new HashMap<>();
//        for (ProductTmp name : names) {
//            if(map.containsKey(name.type)) {
//                int i = map.get(name.type);
//                i = i + 1;
//                map.put(name.type, i);
//            }
//            else {
//                map.put(name.type, 1);
//            }
//        }
//        for (String s : map.keySet()) {
//            if(map.get(s) > 50) System.out.println(s + " : " + map.get(s));
//        }

//        writeCsv(names);


//        ArrayList<ProductTmp> products = read();
//        System.out.println(products.size());
//        for (ProductTmp product : products) {
//            System.out.println(product.createCsv());
//        }

        ArrayList<ProductTmp> names = readProductsFromCsv();
        saveObject(names);
        System.out.println(names.size());
    }

    private static ArrayList<ProductTmp> readProductsFromCsv() throws IOException {
        ArrayList<ProductTmp> names = new ArrayList<>();
        try (Stream<Path> stream = Files.list(Paths.get("data/"))) {
            stream.sorted().filter(path -> String.valueOf(path).endsWith(".csv")).sorted().forEach(file -> {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(file.toFile()));
                    String str = null;
                    while ((str = reader.readLine()) != null) {
                        str = str.trim();
                        if (!str.isEmpty()) {
                            String[] tokens = str.trim().split("~");
                            String name = tokens[0].substring(1, tokens[0].length() - 1);
                            String [] nameTokens = name.split(" ");
                            String specs = nameTokens[nameTokens.length - 1];
                            String type = tokens[1].substring(1, tokens[1].length() - 1);
                            String description = tokens[2].substring(1, tokens[2].length() - 1);
                            String priceString = tokens[3].substring(1, tokens[3].length() - 1);
                            BigDecimal price = BigDecimal.valueOf(Double.valueOf(priceString));
                            String manufacturer = tokens[4].substring(1, tokens[4].length() - 1);
                            ProductTmp product = new ProductTmp();
                            if(specs.contains("ML") || specs.contains("GM")) {
                                product.specs = specs.trim();
                                product.name = name.substring(0, name.indexOf(specs)).trim();
                            }
                            else if(specs.contains("'S")) {
                                if(name.contains("CAPSULE")) {
                                    product.specs = name.substring(name.indexOf("CAPSULE")).trim();
                                    product.name = name.substring(0, name.indexOf("CAPSULE")).trim();
                                }
                                else if(name.contains("TABLET")) {
                                    product.specs = name.substring(name.indexOf("TABLET")).trim();
                                    product.name = name.substring(0, name.indexOf("TABLET")).trim();
                                }
                                else {
                                    product.name = name.trim();
                                    product.specs = " ";
                                }
                            }
                            else {
                                product.name = name.trim();
                                product.specs = " ";
                            }
                            product.type = type;
                            product.description = description;
                            product.price = price;
                            product.manufacturer = manufacturer;
                            names.add(product);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
        return names;
    }

    private static void saveObject(ArrayList<ProductTmp> names) throws IOException {
        FileOutputStream fout = new FileOutputStream(OBJECT_FILE_NAME);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        out.writeObject(names);
        out.flush();
        System.out.println("Object Saved");
    }

    private static ArrayList<ProductTmp> read() throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(OBJECT_FILE_NAME));
        ArrayList<ProductTmp> names = (ArrayList<ProductTmp>) in.readObject();
        return names;
    }

    private static void writeCsv(ArrayList<ProductTmp> products) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_NAME));
        for (ProductTmp product : products) {
            writer.write(product.createCsv()+ "\n");
        }
        writer.close();
        System.out.println("File saved correctly");
    }
}

class ProductTmp implements Serializable {
    String name;
    String specs;
    String type;
    String description;
    BigDecimal price;
    String manufacturer;

    String createCsv() {
        return name + "," + type + "," + specs + "," + description + "," + price + "," + manufacturer;
    }
}
