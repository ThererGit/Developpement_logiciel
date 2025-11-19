import dao.ConnectDB;
import dao.PatientDAO;
import dao.DoctorDAO;
import dao.SpecialtyDAO;
import dao.ConsultationDAO;

import entity.Patient;
import entity.Doctor;
import entity.Specialty;
import entity.Consultation;

import viewmodel.DoctorSearchVM;

import java.time.LocalDate;
import java.time.LocalTime;

public class testDAOs {

    public static void main(String[] args) {

        // --- 1) Instanciation des DAO ---
        PatientDAO patientDAO = new PatientDAO();
        DoctorDAO doctorDAO = new DoctorDAO();
        SpecialtyDAO specialtyDAO = new SpecialtyDAO();
        ConsultationDAO consultationDAO = new ConsultationDAO();

        // --- 2) Test READ : load() ---


        System.out.println("\n=== Specialties ===");
        for (Specialty s : specialtyDAO.load()) {
            System.out.println(s);
        }

        System.out.println("\n=== Doctors ===");
        for (Doctor d : doctorDAO.load()) {
            System.out.println(d);
        }

        System.out.println("\n=== Liste des logins des docteurs ===");
        for (Doctor d : doctorDAO.getList()) {
            System.out.println("Doctor " + d.getLastName() + " → login=" + d.getLogin());
        }


        System.out.println("\n=== Logins via DoctorSearchVM ===");
        for (Doctor d : doctorDAO.getList()) {
            DoctorSearchVM vm = new DoctorSearchVM(d);
            System.out.println(vm.getFullName() + " → login=" + vm.getLogin());
        }
        System.out.println("\n=== TEST LOGIN ===");

        DoctorDAO dao = new DoctorDAO();

// Exemple : login/password à tester
        Doctor d = dao.checkLogin("alice", "password");

        if (d != null) {
            System.out.println("Connexion OK : " + d.getFirstName() + " " + d.getLastName());
        } else {
            System.out.println("Login ou mot de passe incorrect !");
        }


        System.out.println("\n=== Consultations ===");
        for (Consultation c : consultationDAO.load()) {
            System.out.println(c);
        }

        // --- 3) Test CREATE / UPDATE / DELETE sur Patient ---

        System.out.println("\n=== TEST CRUD PATIENT ===");

        // CREATE
        Patient newPatient = new Patient(
                null,
                "TEST_LASTNAME",
                "TEST_FIRSTNAME",
                LocalDate.of(2000, 1, 1)
        );
        patientDAO.save(newPatient);
        System.out.println("Après INSERT, patient = " + newPatient);

        // UPDATE
        newPatient.setLastName("TEST_LASTNAME_UPDATED");
        patientDAO.save(newPatient);
        System.out.println("Après UPDATE, patient = " + newPatient);

        // DELETE
        patientDAO.delete(newPatient);
        System.out.println("Patient supprimé (id=" + newPatient.getId() + ")");


        // --- 4) Test CREATE / DELETE sur Doctor ---

        System.out.println("\n=== TEST CRUD DOCTOR ===");

        // on recharge les spécialités pour être sûr d'en avoir une vraie
        var specs = specialtyDAO.load();
        Specialty firstSpec = specs.isEmpty() ? null : specs.get(0);

        Doctor newDoc = new Doctor(
                null,
                firstSpec,
                "DOC_TEST",
                "DOC_FIRST"
        );
        doctorDAO.save(newDoc);
        System.out.println("Après INSERT, doctor = " + newDoc);

        doctorDAO.delete(newDoc);
        System.out.println("Doctor supprimé (id=" + newDoc.getId() + ")");

        // --- 6) Test CREATE / DELETE sur Consultation ---

        System.out.println("\n=== TEST CRUD CONSULTATION ===");

        // On prend un vrai doctor + un vrai patient existants
        var doctors = doctorDAO.load();
        var patients = patientDAO.load();

        if (!doctors.isEmpty() && !patients.isEmpty()) {
            Doctor doc = doctors.get(0);
            Patient pat = patients.get(0);

            Consultation newCons = new Consultation(
                    null,
                    doc,
                    pat,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(10, 30),
                    "TEST_REASON"
            );
            consultationDAO.save(newCons);
            System.out.println("Après INSERT, consultation = " + newCons);

            consultationDAO.delete(newCons);
            System.out.println("Consultation supprimée (id=" + newCons.getId() + ")");
        } else {
            System.out.println("Pas assez de données pour tester ConsultationDAO (il faut au moins 1 doctor et 1 patient).");
        }

        // --- 7) Fermeture connexion ---
        ConnectDB.close();
    }
}
