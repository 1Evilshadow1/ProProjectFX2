package com.example.proprojectfx.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";  // SID = xe (comme dans ton screenshot)
    private static final String USERNAME = "SYSTEM";       // Change si tu as un autre user
    private static final String PASSWORD = "1590";         // Mets ton mot de passe ici !!

    public static Connection getConnection() {
        try {
            // Le driver est chargé automatiquement avec ojdbc11+
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base Oracle : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Méthode pour tester la connexion (utile pour déboguer)
    public static void testConnection() {
        Connection conn = getConnection();
        if (conn != null) {
            System.out.println("Connexion à Oracle XE réussie !");
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Échec de la connexion.");
        }
    }
}