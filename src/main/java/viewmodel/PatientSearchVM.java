package viewmodel;

import entity.Patient;

import java.time.LocalDate;

public class PatientSearchVM {

    private Patient patient;

    private Integer id;
    private String lastName;
    private String firstName;
    private LocalDate birthDate;

    public PatientSearchVM(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.id = patient.getId();
            this.lastName = patient.getLastName();
            this.firstName = patient.getFirstName();
            this.birthDate = patient.getBirthDate();
        }
    }

    // Pour accéder à l'entité complète si besoin
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.id = patient.getId();
            this.lastName = patient.getLastName();
            this.firstName = patient.getFirstName();
            this.birthDate = patient.getBirthDate();
        }
    }

    // --- Getters utilisés dans les listes / tables de recherche ---

    public Integer getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    @Override
    public String toString() {
        return lastName + " " + firstName + " (" + id + ")";
    }
}
