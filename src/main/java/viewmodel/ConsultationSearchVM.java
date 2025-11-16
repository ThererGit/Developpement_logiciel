package viewmodel;

import entity.Consultation;
import entity.Doctor;
import entity.Patient;
import entity.Specialty;

import java.time.LocalDate;
import java.time.LocalTime;

public class ConsultationSearchVM {

    private Consultation consultation;

    private Integer id;
    private LocalDate date;
    private LocalTime hour;
    private String doctorName;
    private String patientName;
    private String specialtyName;
    private String reason;

    public ConsultationSearchVM(Consultation consultation) {
        this.consultation = consultation;

        if (consultation != null) {
            this.id = consultation.getId();
            this.date = consultation.getDate();
            this.hour = consultation.getHour();
            this.reason = consultation.getReason();

            Doctor doc = consultation.getDoctor();
            if (doc != null) {
                this.doctorName = doc.getLastName() + " " + doc.getFirstName();
                Specialty spec = doc.getSpecialty();
                if (spec != null) {
                    this.specialtyName = spec.getName();
                }
            }

            Patient pat = consultation.getPatient();
            if (pat != null) {
                this.patientName = pat.getLastName() + " " + pat.getFirstName();
            }
        }
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
        // on pourrait recopier la même logique de mise à jour si besoin
    }

    // --- Getters exploités par la vue (liste de consultations) ---

    public Integer getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getHour() {
        return hour;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getSpecialtyName() {
        return specialtyName;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return date + " " + hour + " - " + doctorName +
                (patientName != null ? " / " + patientName : "") +
                (reason != null ? " (" + reason + ")" : "");
    }
}
