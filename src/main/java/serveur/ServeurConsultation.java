package serveur;


import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import ServeurGeneriqueTCP.*;
import protocoleCAP.CAP; import java.io.FileInputStream; import java.io.IOException; import java.util.Properties;


public class ServeurConsultation implements Logger {

    public static void main(String[] args) {
        new ServeurConsultation().start();
    }

    private void start() {
        try {
            Properties props = new Properties();

            try (InputStream input = getClass()
                    .getClassLoader()
                    .getResourceAsStream("server.properties")) {

                if (input == null) {
                    Trace("Erreur : impossible de charger server.properties !");
                    return;
                }

                props.load(input);
            }

            int port = Integer.parseInt(props.getProperty("PORT_CONSULTATION", "50005"));
            int poolSize = Integer.parseInt(props.getProperty("POOL_SIZE", "5"));

            Protocole protocole = new CAP(this);
            ThreadServeur serveur = new ThreadServeurPool(port, protocole, poolSize, this);

            serveur.start();
            Trace("Serveur Consultation démarré sur le port " + port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //test github

    @Override
    public void Trace(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}

