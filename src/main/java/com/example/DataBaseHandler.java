package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseHandler {

    private static final String url = "jdbc:mysql://localhost:3306/invoicesdb";
    private static final String user = "root";
    private static final String password = "password";

    @SuppressWarnings("exports")
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
