package ServeurGeneriqueTCP;

import Network.Requete;
import Network.Reponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class ThreadClient extends Thread {
    protected Protocole protocole;
    protected Socket csocket;
    protected Logger logger;
    private   int numero;

    private static int numCourant = 1;

    public ThreadClient(Protocole protocole, Socket csocket, Logger logger) throws IOException {
        super("TH Client " + numCourant );
        this.protocole = protocole;
        this.csocket = csocket;
        this.logger = logger;
        this.numero = numCourant++;
    }

    public ThreadClient(Protocole protocole, ThreadGroup groupe, Logger logger) throws IOException {
        super(groupe,"TH Client " + numCourant );
        this.protocole = protocole;
        this.csocket = null;
        this.logger = logger;
        this.numero = numCourant++;
    }

    @Override
    public void run() {
        ObjectOutputStream oos = null;
        ObjectInputStream  ois = null;

        try {
            logger.Trace("=== ThreadClient RUN démarré ===");
            logger.Trace("Socket associé : " + csocket);

            // IMPORTANT : création dans cet ordre
            oos = new ObjectOutputStream(csocket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(csocket.getInputStream());

            while (true) {
                logger.Trace("En attente d'un objet depuis le client...");

                Object obj = ois.readObject();
                logger.Trace("Objet reçu : " + obj);

                if (!(obj instanceof Requete req)) {
                    logger.Trace("Reçu objet NON–REQUETE : " + obj.getClass());
                    oos.writeObject("ERREUR : objet non supporté");
                    continue;
                }

                logger.Trace("➡ Requête reçue : commande = "
                        + req.getDemande() + " | data = '" + req.getDonné() + "'");

                // traitement CAP
                Reponse rep = protocole.TraiteRequete(req, csocket);

                logger.Trace("⬅ Réponse générée côté serveur : " + rep);

                oos.writeObject(rep);
                oos.flush();

                logger.Trace("✔ Réponse envoyée au client");
            }
        }
        catch (FinConnexionException ex) {
            logger.Trace("⚠ FinConnexionException : protocole demande coupure");

            // envoyer la dernière réponse si elle existe
            try {
                if (oos != null && ex.getReponse() != null) {
                    logger.Trace("➡ Envoi dernière réponse avant coupure : " + ex.getReponse());
                    oos.writeObject(ex.getReponse());
                    oos.flush();
                }
            } catch (IOException ignored) {}

        }
        catch (IOException ex) {
            logger.Trace("❌ IOException dans ThreadClient : " + ex.getMessage());
        }
        catch (ClassNotFoundException ex) {
            logger.Trace("❌ Erreur : objet non reconnu (pas une Requete).");
        }
        finally {
            try {
                logger.Trace("Fermeture socket client...");
                csocket.close();
            }
            catch (IOException ex) {
                logger.Trace("Erreur fermeture socket : " + ex.getMessage());
            }

            logger.Trace("=== ThreadClient terminé ===");
        }
    }
}
