package com.example.gt;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connexion {
    private static Connection conn = null;
     static  {



         try {
             Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");


             String connectionUrl = "jdbc:sqlserver://DESKTOP-EFVMQLV\\SQLEXPRESS:1433;databaseName=oge-app;user=conn;password=123;trustServerCertificate=true";
            conn = DriverManager.getConnection(connectionUrl);
            System.out.println("Connection established");
        } catch (SQLException e) {
            System.out.println("Connection failed");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
    public static Connection getConnection() {
         return  conn;
    }
}

