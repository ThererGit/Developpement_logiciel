package Network;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean bool;
    private int id;

    private List<Consultation> consultations = new ArrayList<>();

    public Reponse(boolean bool, int id) {
        this.bool = bool;
        this.id = id;
    }

    // ---- Classe interne : une consultation ----
    public static class Consultation implements Serializable {
        private static final long serialVersionUID = 1L;

        private int idcons;
        private String nom;
        private String prenom;
        private LocalDateTime date;
        private String raison;

        public Consultation(int id ,String nom, String prenom, LocalDateTime  date, String raison) {
            this.idcons = id;
            this.nom = emptyToNull(nom);
            this.prenom = emptyToNull(prenom);
            this.date = date;
            this.raison = emptyToNull(raison);
        }

        private static String emptyToNull(String s) {
            if (s == null || s.trim().isEmpty()) return null;
            return s;
        }

        public int getIdcons() { return idcons; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public LocalDateTime getDate() { return date; }
        public String getRaison() { return raison; }

        @Override
        public String toString() {
            return "Consultation{" + "idcons" + idcons +
                    "nom='" + nom + '\'' +
                    ", prenom='" + prenom + '\'' +
                    ", date='" + date + '\'' +
                    ", raison='" + raison + '\'' +
                    '}';
        }
    }

    // ---- Méthodes Reponse ----

    public boolean isBool() { return bool; }
    public int getId() { return id; }
    public List<Consultation> getConsultations() { return consultations; }

    public void addConsultation(int idcons, String nom, String prenom, LocalDateTime date, String raison) {
        consultations.add(new Consultation(idcons ,nom, prenom, date, raison));
    }

    public void addConsultation(Consultation c) {
        consultations.add(c);
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "bool=" + bool +
                ", id=" + id +
                ", consultations=" + consultations +
                '}';
    }
}