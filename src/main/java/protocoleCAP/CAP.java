package protocoleCAP;

import Network.Requete;
import Network.Reponse;
import ServeurGeneriqueTCP.*;

import dao.*;
import entity.*;

import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CAP implements Protocole {

    private final Logger logger;
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final ConsultationDAO consultationDAO = new ConsultationDAO();

    // un doctor par socket
    private final Map<Socket, Doctor> doctorsConnectes =
            Collections.synchronizedMap(new HashMap<>());

    public CAP(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String getNom() {
        return "CAP";
    }

    /** Petite fonction utilitaire pour nettoyer les données reçues */
    private String cleanData(String data) {
        if (data == null) return "";
        data = data.trim();
        while (data.endsWith(".")) {
            data = data.substring(0, data.length() - 1);
        }
        return data.trim();
    }

    /** Parse une date/heure envoyée par le client.
     *  Accepte :
     *    - format ISO : 2025-10-02T14:20
     *    - format avec espace : 2025-10-02 14:20
     *  Retourne null si aucun format ne passe.
     */
    private LocalDateTime parseClientDateTime(String s) {
        if (s == null) return null;
        s = s.trim();

        // 1) Essayer format ISO (LocalDateTime.toString())
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e1) {
            // 2) Essayer avec espace : "yyyy-MM-dd HH:mm"
            try {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return LocalDateTime.parse(s, f);
            } catch (DateTimeParseException e2) {
                logger.Trace("[CAP] parseClientDateTime -> format invalide pour '" + s + "'");
                return null;
            }
        }
    }


    @Override
    public Reponse TraiteRequete(Requete req, Socket socket) throws FinConnexionException {
        String cmd  = req.getDemande();
        String data = req.getDonné() == null ? "" : req.getDonné();

        logger.Trace("=== TraiteRequete ===");
        logger.Trace("Socket : " + socket);
        logger.Trace("Commande brute : '" + cmd + "'");
        logger.Trace("Data brute     : '" + data + "'");

        // LOG IMPORTANT : requête exacte reçue du client
        logger.Trace("[CAP] >>> Requête reçue du client : cmd='" + cmd + "', data='" + data + "'");

        System.out.println("[CAP] Requête reçue -> cmd='" + cmd + "', data='" + data + "'");

        switch (cmd) {
            case "LOGIN" -> {
                Reponse r = handleLogin(data, socket);
                logger.Trace("Réponse LOGIN : success=" + r.isSucces() + ", code=" + r.getCode());
                System.out.println("[CAP] Fin LOGIN, succes=" + r.isSucces() + ", code=" + r.getCode());
                return r;
            }
            case "LOGOUT" -> {
                logger.Trace("LOGOUT reçu, suppression du doctor pour socket " + socket);
                System.out.println("[CAP] LOGOUT -> fermeture de la connexion pour " + socket);
                doctorsConnectes.remove(socket);
                throw new FinConnexionException(new Reponse(true, 0));
            }
        }

        Doctor doctor = doctorsConnectes.get(socket);
        if (doctor == null) {
            logger.Trace("Aucun doctor associé à cette socket -> FinConnexionException");
            System.out.println("[CAP] Avertissement : commande '" + cmd + "' sans doctor connecté !");
            throw new FinConnexionException(new Reponse(false, -1));
        }

        logger.Trace("Doctor courant : id=" + doctor.getId() + ", nom=" + doctor.getLastName());
        System.out.println("[CAP] Doctor courant : id=" + doctor.getId() + ", nom=" + doctor.getLastName());

        Reponse rep;

        switch (cmd) {
            case "ADD_PATIENT" -> rep = handleAddPatient(data);
            case "ADD_CONSULTATION" -> rep = handleAddConsultation(data, doctor);
            case "UPDATE_CONSULTATION" -> rep = handleUpdateConsultation(req, data, doctor);
            case "SEARCH_CONSULTATIONS" -> rep = handleSearchConsultations(data, doctor);
            case "DELETE_CONSULTATION" -> rep = handleDeleteConsultation(data, doctor);
            default -> {
                logger.Trace("Commande inconnue : " + cmd);
                System.out.println("[CAP] Commande inconnue : '" + cmd + "'");
                rep = new Reponse(false, 0);
            }
        }

        logger.Trace("Réponse envoyée -> success=" + rep.isSucces() + ", code=" + rep.getCode());
        System.out.println("[CAP] Réponse envoyée pour cmd='" + cmd + "' -> succes=" + rep.isSucces() + ", code=" + rep.getCode());

        return rep;
    }


    // ---------------- LOGIN ----------------

    private Reponse handleLogin(String data, Socket socket) {
        logger.Trace("=== handleLogin ===");
        logger.Trace("Data brute LOGIN : '" + data + "'");
        System.out.println("[CAP] handleLogin data brute : '" + data + "'");

        data = cleanData(data);

        logger.Trace("Data nettoyée LOGIN : '" + data + "'");
        System.out.println("[CAP] handleLogin data nettoyée : '" + data + "'");

        // 2) Split sur la virgule : login,password
        String[] p = data.split(",", -1);
        logger.Trace("LOGIN split : " + Arrays.toString(p));
        if (p.length < 2) {
            logger.Trace("LOGIN : format invalide, il faut 'login,password'");
            System.out.println("[CAP] LOGIN format invalide : " + data);
            return new Reponse(false, -1);
        }

        // 3) Trim des deux champs
        String login    = p[0].trim();
        String password = p[1].trim();

        logger.Trace("Tentative de login pour : " + login);
        System.out.println("[CAP] Tentative de login pour : " + login);

        // 4) Vérification via le DAO
        Doctor d = doctorDAO.checkLogin(login, password);

        if (d != null) {
            doctorsConnectes.put(socket, d);
            logger.Trace("Doctor connecté : " + d.getLastName() + " (id=" + d.getId() + ", login=" + login + ")");
            System.out.println("[CAP] LOGIN OK -> doctor id=" + d.getId() + ", nom=" + d.getLastName());
            return new Reponse(true, d.getId());
        }

        logger.Trace("Login refusé pour " + login);
        System.out.println("[CAP] LOGIN refusé pour " + login);
        return new Reponse(false, -1);
    }


    // ---------------- ADD_PATIENT ----------------

    private Reponse handleAddPatient(String data) {
        logger.Trace("=== handleAddPatient ===");
        logger.Trace("Data brute : '" + data + "'");
        System.out.println("[CAP] handleAddPatient data brute : '" + data + "'");

        data = cleanData(data);

        logger.Trace("Data nettoyée : '" + data + "'");
        System.out.println("[CAP] handleAddPatient data nettoyée : '" + data + "'");

        String[] p = data.split(",", -1);
        logger.Trace("ADD_PATIENT split : " + Arrays.toString(p));
        if (p.length < 2) {
            logger.Trace("ADD_PATIENT : format invalide");
            System.out.println("[CAP] ADD_PATIENT format invalide");
            return new Reponse(false, 0);
        }

        String nom = p[0].trim();
        String prenom = p[1].trim();

        logger.Trace("Création patient nom='" + nom + "', prenom='" + prenom + "'");
        System.out.println("[CAP] Création patient : " + nom + " " + prenom);

        Patient pat = new Patient(null, nom, prenom, null);
        patientDAO.save(pat);

        logger.Trace("Patient créé avec id=" + pat.getId());
        System.out.println("[CAP] Patient créé id=" + pat.getId());

        return new Reponse(true, pat.getId());
    }

    // ---------------- ADD_CONSULTATION ----------------

    private Reponse handleAddConsultation(String data, Doctor doctor) {

        logger.Trace("=== handleAddConsultation ===");
        logger.Trace("Data brute : '" + data + "'");
        System.out.println("[CAP] handleAddConsultation data brute : '" + data + "'");

        data = cleanData(data);

        logger.Trace("Data nettoyée : '" + data + "'");
        System.out.println("[CAP] handleAddConsultation data nettoyée : '" + data + "'");

        String[] p = data.split(",", -1);
        logger.Trace("ADD_CONSULTATION split : " + Arrays.toString(p));
        if (p.length < 3) {
            logger.Trace("ADD_CONSULTATION : format invalide");
            System.out.println("[CAP] ADD_CONSULTATION format invalide");
            return new Reponse(false, 0);
        }

        // ⬇⬇⬇ ici : on utilise la fonction tolérante ⬇⬇⬇
        LocalDateTime debut = parseClientDateTime(p[0]);
        if (debut == null) {
            logger.Trace("ADD_CONSULTATION : date/heure invalide '" + p[0] + "'");
            return new Reponse(false, 0);
        }

        int duree = Integer.parseInt(p[1].trim());
        int nb = Integer.parseInt(p[2].trim());

        logger.Trace("debut=" + debut + ", duree=" + duree + ", nb=" + nb);
        System.out.println("[CAP] Plage de consultations : debut=" + debut + ", duree=" + duree + ", nb=" + nb);

        LocalDateTime fin = debut.plusMinutes((long) duree * nb);
        if (fin.getHour() > 17 || (fin.getHour() == 17 && fin.getMinute() > 0)) {
            logger.Trace("ADD_CONSULTATION : dépassement de 17h");
            System.out.println("[CAP] ADD_CONSULTATION refusée : dépassement de 17h");
            return new Reponse(false, 0);
        }

        for (int i = 0; i < nb; i++) {
            LocalDateTime dh = debut.plusMinutes((long) duree * i);

            Consultation c = new Consultation(
                    null,
                    doctor,
                    null,
                    dh.toLocalDate(),
                    dh.toLocalTime(),
                    null
            );
            consultationDAO.save(c);
            logger.Trace("Consultation créée id=" + c.getId() + " à " + dh);
            System.out.println("[CAP] Consultation créée id=" + c.getId() + " à " + dh);
        }

        return new Reponse(true, 0);
    }

    // ---------------- UPDATE_CONSULTATION ----------------

    private Reponse handleUpdateConsultation(Requete req, String data, Doctor doctor) {

        logger.Trace("=== handleUpdateConsultation ===");
        logger.Trace("Data brute : '" + data + "'");
        System.out.println("[CAP] handleUpdateConsultation data brute : '" + data + "'");

        data = cleanData(data);

        logger.Trace("Data nettoyée : '" + data + "'");
        System.out.println("[CAP] handleUpdateConsultation data nettoyée : '" + data + "'");

        String[] p = data.split(",", -1);
        if (p.length < 1) {
            logger.Trace("UPDATE_CONSULTATION : format invalide (aucun champ)");
            return new Reponse(false, 0);
        }

        // id consultation obligatoire
        int id;
        try {
            id = Integer.parseInt(p[0].trim());
        } catch (NumberFormatException e) {
            logger.Trace("UPDATE_CONSULTATION : id consultation non numérique '" + p[0] + "'");
            return new Reponse(false, 0);
        }

        // champs optionnels
        String nom    = (p.length > 1) ? p[1].trim() : "";
        String prenom = (p.length > 2) ? p[2].trim() : "";
        String raison = (p.length > 3) ? p[3].trim() : "";

        // date obligatoire dans l'objet Requete
        LocalDateTime nv = req.getDate();
        if (nv == null) {
            logger.Trace("UPDATE_CONSULTATION : date absente dans Requete");
            return new Reponse(false, id);
        }

        logger.Trace("UPDATE_CONSULTATION id=" + id +
                ", nv=" + nv +
                ", nom='" + nom + "'" +
                ", prenom='" + prenom + "'" +
                ", raison='" + raison + "'");

        Consultation c = consultationDAO.getById(id);
        if (c == null || !c.getDoctor().getId().equals(doctor.getId())) {
            logger.Trace("UPDATE_CONSULTATION : consultation introuvable ou doctor différent");
            return new Reponse(false, id);
        }

        // ===== Mise à jour de la date =====
        c.setDate(nv.toLocalDate());
        c.setHour(nv.toLocalTime());

        // ===== Mise à jour de la raison si fournie =====
        if (!raison.isBlank()) {
            c.setReason(raison);
        }

        // ===== Mise à jour du patient seulement si nom/prenom non vides =====
        if (!nom.isBlank() || !prenom.isBlank()) {
            Patient patient = patientDAO.findByNameAndFirstName(nom, prenom);
            if (patient == null) {
                logger.Trace("UPDATE_CONSULTATION : patient introuvable " + nom + " " + prenom);
                return new Reponse(false, id);
            }
            c.setPatient(patient);
        } // sinon : on garde le patient actuel

        consultationDAO.save(c);

        logger.Trace("UPDATE_CONSULTATION OK pour id=" + id);
        System.out.println("[CAP] UPDATE_CONSULTATION OK pour id=" + id);
        return new Reponse(true, id);
    }




    // ---------------- SEARCH_CONSULTATIONS ----------------

    private Reponse handleSearchConsultations(String data, Doctor doctor) {

        logger.Trace("=== handleSearchConsultations ===");
        logger.Trace("Data brute : '" + data + "'");

        data = cleanData(data);

        logger.Trace("Data nettoyée : '" + data + "'");

        Integer idPatient = null;
        LocalDate date = null;

        // Cas client : "all." ou "all" ou chaîne vide -> aucun filtre
        if (data.isEmpty() || data.equalsIgnoreCase("all")) {
            logger.Trace("SEARCH_CONSULTATIONS : aucun filtre (ALL)");
        } else {
            // Ancien format : "idPatient,date"
            String[] p = data.split(",", -1);
            logger.Trace("SEARCH_CONSULTATIONS split : " + Arrays.toString(p));

            // patient
            if (p.length > 0 && !p[0].isBlank() &&
                    !p[0].equals("0") &&
                    !p[0].equalsIgnoreCase("all")) {
                try {
                    idPatient = Integer.parseInt(p[0].trim());
                    logger.Trace("Filtre idPatient=" + idPatient);
                } catch (NumberFormatException e) {
                    logger.Trace("idPatient non numérique : '" + p[0] + "', filtre patient ignoré");
                    idPatient = null;
                }
            }

            // date
            if (p.length > 1 && !p[1].isBlank()) {
                try {
                    date = LocalDate.parse(p[1].trim());
                    logger.Trace("Filtre date=" + date);
                    System.out.println("[CAP] Filtre date=" + date);
                } catch (Exception e) {
                    logger.Trace("Date invalide : '" + p[1] + "', filtre date ignoré");
                    System.out.println("[CAP] ATTENTION : date invalide '" + p[1] + "', on ignore le filtre date");
                    date = null;
                }
            }
        }

        consultationDAO.load();
        List<Consultation> all = consultationDAO.getList();

        logger.Trace("Nombre total de consultations en base : " + all.size());
        System.out.println("[CAP] consultations en base : " + all.size());

        Reponse rep = new Reponse(true, 0);
        int count = 0;

        for (Consultation c : all) {
            logger.Trace("Examen consultation id=" + c.getId() +
                    ", doctor=" + c.getDoctor().getId() +
                    ", patient=" + (c.getPatient() != null ? c.getPatient().getId() : "null") +
                    ", date=" + c.getDate());

            if (!c.getDoctor().getId().equals(doctor.getId())) {
                logger.Trace(" -> ignorée (autre doctor)");
                continue;
            }

            if (idPatient != null) {
                if (c.getPatient() == null ||
                        !c.getPatient().getId().equals(idPatient)) {
                    logger.Trace(" -> ignorée (patient différent)");
                    continue;
                }
            }

            if (date != null && !c.getDate().equals(date)) {
                logger.Trace(" -> ignorée (date différente)");
                continue;
            }

            String nom = c.getPatient() != null ? c.getPatient().getLastName() : null;
            String prenom = c.getPatient() != null ? c.getPatient().getFirstName() : null;

            LocalDateTime dh = LocalDateTime.of(c.getDate(), c.getHour());
            rep.addConsultation(
                    c.getId(),
                    nom,
                    prenom,
                    dh,
                    c.getReason()
            );
            count++;
            logger.Trace(" -> ajoutée à la réponse (id=" + c.getId() + ")");
        }

        logger.Trace("SEARCH_CONSULTATIONS : " + count + " consultation(s) trouvée(s)");


        return rep;
    }

    // ---------------- DELETE_CONSULTATION ----------------

    private Reponse handleDeleteConsultation(String data, Doctor doctor) {

        logger.Trace("=== handleDeleteConsultation ===");
        logger.Trace("[CAP] handleDeleteConsultation data brute : '" + data + "'");

        data = cleanData(data);

        logger.Trace("[CAP] handleDeleteConsultation data nettoyée : '" + data + "'");


        int id = Integer.parseInt(data.trim());
        logger.Trace("Demande suppression consultation id=" + id);

        Consultation c = consultationDAO.getById(id);
        if (c == null ||
                !c.getDoctor().getId().equals(doctor.getId()) ||
                c.getPatient() != null) {

            logger.Trace("DELETE_CONSULTATION refusée pour id=" + id);
            System.out.println("[CAP] DELETE_CONSULTATION refusée pour id=" + id);
            return new Reponse(false, id);
        }

        consultationDAO.delete(c);
        logger.Trace("DELETE_CONSULTATION OK pour id=" + id);
        return new Reponse(true, id);
    }
}
