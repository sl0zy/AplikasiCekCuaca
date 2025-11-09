/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cekcuaca.service;


import com.cekcuaca.model.WeatherData;
import java.io.*;
import java.util.*;

/**
 *
 * @author slozoy
 */
public class FileService {
    // Simpan data ke file CSV
    public static void saveToCSV(List<WeatherData> list, File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("City,Main,Description,Temperature,Humidity"); // Tambahkan kolom Main
            for (WeatherData data : list) {
                pw.printf("%s,%s,%s,%.2f,%.2f%n", 
                    data.getCity(),
                    data.getMain(),
                    data.getDescription(),
                    data.getTemperature(),
                    data.getHumidity());
            }
        }
    }

    // Muat data dari file CSV
    public static List<WeatherData> loadFromCSV(File file) throws IOException {
        List<WeatherData> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 5) { // pastikan jumlah kolom cukup
                    list.add(new WeatherData(
                        p[0],                // city
                        p[1],                // main
                        p[2],                // description
                        Double.parseDouble(p[3]), // temperature
                        Double.parseDouble(p[4])  // humidity
                    ));
                }
            }
        }
        return list;
    }
}
