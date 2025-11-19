package ServeurGeneriqueTCP;

import Network.Reponse;

public class FinConnexionException extends Exception {
    private final Reponse reponse;

    public FinConnexionException() {
        this(null);
    }

    public FinConnexionException(Reponse reponse) {
        this.reponse = reponse;
    }

    public Reponse getReponse() {
        return reponse;
    }
}
