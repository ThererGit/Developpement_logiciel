package Network;

import java.io.Serializable;

public class Requete implements Serializable {
    private String demande;
    private String donné;


    public Requete(String donné, String demande) {
        this.donné = donné;
        this.demande = demande;
    }

    public String getDemande() {
        return demande;
    }

    public String getDonné() {
        return donné;
    }
}
