/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cekcuaca.util;

import javax.swing.*;
import java.net.URL;
/**
 *
 * @author slozoy
 */
public class WeatherIconLoader {
    public static ImageIcon getIcon(String main) {
        // Path default icon
        String defaultIconPath = "/icons/default.png";
        String iconPath = defaultIconPath;

        // Jika main kosong/null → langsung pakai default
        if (main == null || main.isEmpty()) {
            System.out.println("Main weather data kosong, gunakan ikon default.");
            return loadIcon(defaultIconPath);
        }

        // Pilih ikon berdasarkan kondisi cuaca
        switch (main.toLowerCase()) {
            case "clear" -> iconPath = "/icons/clear.png";
            case "clouds" -> iconPath = "/icons/clouds.png";
            case "rain" -> iconPath = "/icons/rain.png";
            case "snow" -> iconPath = "/icons/snow.png";
            case "mist" -> iconPath = "/icons/mist.png";
            case "fog" -> iconPath = "/icons/fog.png";
            case "smoke" -> iconPath = "/icons/smoke.png";
            case "haze" -> iconPath = "/icons/haze.png";
            case "thunderstorm" -> iconPath = "/icons/thunderstorm.png";
            case "drizzle" -> iconPath = "/icons/drizzle.png";
            case "dust" -> iconPath = "/icons/dust.png";
            case "sand" -> iconPath = "/icons/sand.png";
            case "tornado" -> iconPath = "/icons/tornado.png";
            default -> {
                System.out.println("Tidak ada ikon untuk kondisi: " + main + ", gunakan default.");
                iconPath = defaultIconPath;
            }
        }

        return loadIcon(iconPath);
    }

    private static ImageIcon loadIcon(String path) {
        URL url = WeatherIconLoader.class.getResource(path);
        if (url == null) {
            System.err.println("Icon file tidak ditemukan: " + path + ", gunakan ikon default.");
            url = WeatherIconLoader.class.getResource("/icons/default.png");
        }
        return new ImageIcon(url);
    }
}
