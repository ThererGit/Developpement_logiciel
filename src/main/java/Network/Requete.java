package Network;

import java.time.LocalDateTime;

import java.io.Serializable;

public class Requete implements Serializable {
    private String demande;
    private String donné;
    private LocalDateTime date;


    public Requete(String donné, String demande, LocalDateTime date) {
        this.donné = donné;
        this.date = date;
        this.demande = demande;
    }

    public Requete(String donné, String demande) {
        this.donné = donné;
        this.date = null;
        this.demande = demande;
    }

    public String getDemande() {
        return demande;
    }

    public String getDonné() {
        return donné;
    }

    public LocalDateTime getDate() {
        return date;
    }
}