/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cekcuaca.service;


import com.cekcuaca.model.WeatherData;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author slozoy
 */
public class WeatherService {
    private static final String API_KEY = "6abc6339f80be76370c670697cd93257"; // Ganti dengan API key kamu

    public static WeatherData getWeather(String city) throws Exception {
        String cityName = city.trim(); // versi asli untuk label
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8); // versi aman untuk URL

        String endpoint = "https://api.openweathermap.org/data/2.5/weather?q=" 
                + encodedCity + "&units=metric&lang=id&appid=" + API_KEY;

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        InputStreamReader reader = new InputStreamReader(conn.getInputStream());
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) sb.append((char) ch);
        reader.close();

        JSONObject json = new JSONObject(sb.toString());

        String main = json.getJSONArray("weather").getJSONObject(0).getString("main");
        String description = json.getJSONArray("weather").getJSONObject(0).getString("description");
        double temp = json.getJSONObject("main").getDouble("temp");
        double humidity = json.getJSONObject("main").getDouble("humidity");

        // cityName tetap versi original, bukan encoded
        return new WeatherData(cityName, main, description, temp, humidity);
    }
}
