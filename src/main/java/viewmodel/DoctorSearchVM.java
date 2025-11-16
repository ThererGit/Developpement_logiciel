package viewmodel;

import entity.Doctor;
import entity.Specialty;

public class DoctorSearchVM {

    private Doctor doctor;

    private Integer id;
    private String lastName;
    private String firstName;
    private String specialtyName;

    public DoctorSearchVM(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            this.id = doctor.getId();
            this.lastName = doctor.getLastName();
            this.firstName = doctor.getFirstName();

            Specialty spec = doctor.getSpecialty();
            if (spec != null) {
                this.specialtyName = spec.getName();
            } else {
                this.specialtyName = null;
            }
        }
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            this.id = doctor.getId();
            this.lastName = doctor.getLastName();
            this.firstName = doctor.getFirstName();

            Specialty spec = doctor.getSpecialty();
            if (spec != null) {
                this.specialtyName = spec.getName();
            } else {
                this.specialtyName = null;
            }
        }
    }

    // champs utilisés par la vue

    public Integer getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSpecialtyName() {
        return specialtyName;
    }

    public String getFullName() {
        return lastName + " " + firstName;
    }

    @Override
    public String toString() {
        if (specialtyName != null && !specialtyName.isEmpty()) {
            return lastName + " " + firstName + " - " + specialtyName;
        } else {
            return lastName + " " + firstName;
        }
    }
}
