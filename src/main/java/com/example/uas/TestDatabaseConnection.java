package com.example.uas;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        try (Connection connection = Database.getConnection()) {
            System.out.println("Koneksi berhasil!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

