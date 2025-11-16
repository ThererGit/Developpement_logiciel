package serveur;

import Network.Requete;
import Network.Reponse;
import dao.*;   // tes DAO
import entity.*;


import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServeurConsultation {

    public static void main(String[] args) {
        new ServeurConsultation().start();
    }

    private void start() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("server.properties"));

            int port = Integer.parseInt(props.getProperty("PORT_CONSULTATION", "50000"));
            int poolSize = Integer.parseInt(props.getProperty("POOL_SIZE", "5"));

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Serveur Consultation démarré sur le port " + port);

            ExecutorService pool = Executors.newFixedThreadPool(poolSize);

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Nouveau client : " + client.getRemoteSocketAddress());
                pool.submit(new ClientHandler(client));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
