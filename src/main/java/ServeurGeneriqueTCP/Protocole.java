package ServeurGeneriqueTCP;

import Network.Requete;
import Network.Reponse;
import java.net.Socket;

public interface Protocole {
    String getNom();
    Reponse TraiteRequete(Requete req, Socket socket) throws FinConnexionException;
}
