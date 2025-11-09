/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cekcuaca.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author najwa
 */
public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:weather.db";

    static {
        createTableIfNotExists();
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void createTableIfNotExists() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS favorite_locations (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "city_name TEXT UNIQUE NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addFavorite(String city) throws SQLException {
        String sql = "INSERT OR IGNORE INTO favorite_locations (city_name) VALUES (?)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, city);
            ps.executeUpdate();
        }
    }

    public static List<String> getAllFavorites() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT city_name FROM favorite_locations ORDER BY city_name ASC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("city_name"));
            }
        }
        return list;
    }
}
