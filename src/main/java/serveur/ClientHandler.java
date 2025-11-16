package serveur;

import Network.Requete;
import Network.Reponse;
import dao.*;
import entity.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final ConsultationDAO consultationDAO = new ConsultationDAO();

    private Doctor doctorConnecte = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object obj;

                try {
                    obj = ois.readObject();
                } catch (EOFException e) {
                    System.out.println("Client déconnecté (EOF).");
                    break;
                }

                if (!(obj instanceof Requete req)) {
                    sendError("Objet inconnu reçu");
                    continue;
                }

                Reponse rep = traite(req);
                if (rep != null) {
                    oos.writeObject(rep);
                    oos.flush();
                }
            }

        } catch (Exception e) {
            System.out.println("Erreur ClientHandler : " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void sendError(String msg) {
        try {
            oos.writeObject(msg);
            oos.flush();
        } catch (IOException ignored) {}
    }

    // -------------------------------------------------------------------------
    // DISPATCH DES COMMANDES
    // -------------------------------------------------------------------------

    private Reponse traite(Requete req) {
        String cmd = req.getDemande();
        String data = req.getDonné() == null ? "" : req.getDonné();

        switch (cmd) {
            case "LOGIN": return handleLogin(data);
            case "LOGOUT": return handleLogout();
        }

        // toutes les autres commandes nécessitent un doctor connecté
        if (doctorConnecte == null) {
            sendError("Non authentifié");
            return null;
        }

        return switch (cmd) {
            case "ADD_PATIENT" -> handleAddPatient(data);
            case "ADD_CONSULTATION" -> handleAddConsultation(data);
            case "UPDATE_CONSULTATION" -> handleUpdateConsultation(data);
            case "SEARCH_CONSULTATIONS" -> handleSearchConsultations(data);
            case "DELETE_CONSULTATION" -> handleDeleteConsultation(data);
            default -> {
                sendError("Commande inconnue : " + cmd);
                yield null;
            }
        };
    }

    // -------------------------------------------------------------------------
    // LOGIN / LOGOUT
    // -------------------------------------------------------------------------

    private Reponse handleLogin(String data) {
        String[] p = data.split(";", -1);
        if (p.length < 2) return new Reponse(false, -1);

        String login = p[0];
        String password = p[1];

        // ✦ DoctorDAO n’a pas findByLoginPassword → on charge tout et on filtre
        List<Doctor> liste = doctorDAO.load();

        for (Doctor d : liste) {
            if (d.getLastName().equals(login) && d.getFirstName().equals(password)) {
                doctorConnecte = d;
                return new Reponse(true, d.getId());
            }
        }

        return new Reponse(false, -1);
    }

    private Reponse handleLogout() {
        doctorConnecte = null;
        return new Reponse(true, 0);
    }

    // -------------------------------------------------------------------------
    // PATIENT
    // -------------------------------------------------------------------------

    private Reponse handleAddPatient(String data) {
        String[] p = data.split(";", -1);
        if (p.length < 2) return new Reponse(false, 0);

        String nom = p[0];
        String prenom = p[1];

        Patient pat = new Patient(null, nom, prenom, null);
        patientDAO.save(pat);

        return new Reponse(true, pat.getId());
    }

    // -------------------------------------------------------------------------
    // ADD CONSULTATION
    // -------------------------------------------------------------------------

    private Reponse handleAddConsultation(String data) {
        String[] p = data.split(";", -1);
        if (p.length < 3) return new Reponse(false, 0);

        LocalDateTime debut = LocalDateTime.parse(p[0]);
        int duree = Integer.parseInt(p[1]);
        int nb = Integer.parseInt(p[2]);

        LocalDateTime fin = debut.plusMinutes((long) duree * nb);
        if (fin.getHour() > 17 || (fin.getHour() == 17 && fin.getMinute() > 0)) {
            return new Reponse(false, 0);
        }

        for (int i = 0; i < nb; i++) {
            LocalDateTime dh = debut.plusMinutes((long) duree * i);

            Consultation c = new Consultation(
                    null,
                    doctorConnecte,
                    null,
                    dh.toLocalDate(),
                    dh.toLocalTime(),
                    null
            );

            consultationDAO.save(c);
        }

        return new Reponse(true, 0);
    }

    // -------------------------------------------------------------------------
    // UPDATE CONSULTATION
    // -------------------------------------------------------------------------

    private Reponse handleUpdateConsultation(String data) {
        String[] p = data.split(";", -1);
        if (p.length < 2) return new Reponse(false, 0);

        int id = Integer.parseInt(p[0]);
        LocalDateTime nv = LocalDateTime.parse(p[1]);

        Consultation c = consultationDAO.getById(id);
        if (c == null || c.getDoctor().getId() != doctorConnecte.getId()) {
            return new Reponse(false, id);
        }

        c.setDate(nv.toLocalDate());
        c.setHour(nv.toLocalTime());

        consultationDAO.save(c); // UPDATE intégré

        return new Reponse(true, id);
    }

    // -------------------------------------------------------------------------
    // SEARCH CONSULTATIONS
    // -------------------------------------------------------------------------

    private Reponse handleSearchConsultations(String data) {

        String[] p = data.split(";", -1);
        Integer idPatient = null;
        LocalDate date = null;

        if (!p[0].isBlank() && !p[0].equals("0"))
            idPatient = Integer.parseInt(p[0]);

        if (p.length > 1 && !p[1].isBlank())
            date = LocalDate.parse(p[1]);

        // ✦ consultationDAO n’a pas de méthode searchForDoctor → requête SQL directe
        consultationDAO.load(); // recharge depuis la BD
        List<Consultation> all = consultationDAO.getList();

        Reponse rep = new Reponse(true, 0);

        for (Consultation c : all) {

            if (!c.getDoctor().getId().equals(doctorConnecte.getId()))
                continue;

            if (idPatient != null) {
                if (c.getPatient() == null ||
                        !c.getPatient().getId().equals(idPatient)) {
                    continue;
                }
            }

            if (date != null && !c.getDate().equals(date))
                continue;

            String nom = c.getPatient() != null ? c.getPatient().getLastName() : null;
            String prenom = c.getPatient() != null ? c.getPatient().getFirstName() : null;

            rep.addConsultation(
                    c.getId(),
                    nom,
                    prenom,
                    LocalDateTime.of(c.getDate(), c.getHour()),
                    c.getReason()
            );
        }

        return rep;
    }

    // -------------------------------------------------------------------------
    // DELETE CONSULTATION
    // -------------------------------------------------------------------------

    private Reponse handleDeleteConsultation(String data) {
        int id = Integer.parseInt(data.trim());

        Consultation c = consultationDAO.getById(id);

        if (c == null ||
                c.getDoctor().getId() != doctorConnecte.getId() ||
                c.getPatient() != null) {

            return new Reponse(false, id);
        }

        consultationDAO.delete(c);

        return new Reponse(true, id);
    }
}
