package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectDB {

    private static Connection conn = null;
    private static Properties props = null;

    public ConnectDB() {
        try {
            // Charger les propriétés une seule fois
            if (props == null) {
                props = new Properties();
                // Chemin selon ton projet (à adapter si besoin)
                FileInputStream fis = new FileInputStream("server.properties");
                props.load(fis);
            }

            // Ouvrir la connexion si absente ou fermée
            if (conn == null || conn.isClosed()) {

                String url  = props.getProperty("DB_URL");
                String user = props.getProperty("DB_USER");
                String pass = props.getProperty("DB_PASS");

                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(url, user, pass);

                System.out.println("Connexion DB ouverte → " + url);
            }

        } catch (IOException | ClassNotFoundException | SQLException ex) {
            Logger.getLogger(ConnectDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConn() {
        return conn;
    }

    public static Properties getProperties() {
        return props;
    }

    public static void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Connexion DB fermée");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConnectDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
