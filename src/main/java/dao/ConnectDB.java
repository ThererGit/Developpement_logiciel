package dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectDB {

    private static Connection conn = null;
    private static Properties props = null;

    public ConnectDB() {
        try {
            // 1. Charger les propriétés une seule fois
            if (props == null) {
                loadPropertiesFromClasspath();
            }

            // 2. Ouvrir la connexion si nécessaire
            if (conn == null || conn.isClosed()) {
                String url  = props.getProperty("DB_URL");
                String user = props.getProperty("DB_USER");
                String pass = props.getProperty("DB_PASS");

                if (url == null || user == null || pass == null) {
                    throw new RuntimeException("Propriétés DB_URL / DB_USER / DB_PASS manquantes dans server.properties");
                }

                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(url, user, pass);
                System.out.println("Connexion DB ouverte → " + url);
            }
        }
        catch (ClassNotFoundException | SQLException | IOException ex) {
            Logger.getLogger(ConnectDB.class.getName()).log(Level.SEVERE, null, ex);
            // On préfère stopper plutôt que laisser conn = null et avoir des NPE plus loin
            throw new RuntimeException("Erreur lors de l'initialisation de ConnectDB", ex);
        }
    }

    private void loadPropertiesFromClasspath() throws IOException {
        props = new Properties();

        // On cherche server.properties dans le classpath
        try (InputStream is = ConnectDB.class.getClassLoader()
                .getResourceAsStream("server.properties")) {
            if (is == null) {
                throw new IOException("server.properties introuvable dans le classpath");
            }
            props.load(is);
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
                System.out.println("Closing DB connection");
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(ConnectDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
