package com.restoran;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBHelper {

    private static final String URL = "jdbc:postgresql://localhost:5432/RestoranDB";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123";

    public static Connection baglan() {
        Connection conn = null;
        try {

            Class.forName("org.postgresql.Driver");

            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Veritabanına bağlandık.");

        } catch (ClassNotFoundException e) {
            System.out.println("SÜRÜCÜ YOK: Maven yükleyememiş.");
        } catch (Exception e) {
            System.out.println("BAĞLANTI HATASI: " + e.getMessage());
        }
        return conn;
    }
}