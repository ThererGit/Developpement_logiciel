package entity;


import java.time.LocalDate;
import java.time.LocalTime;

public class Consultation {
    private Integer id;
    private Doctor doctor;
    private Patient patient; // peut être null
    private LocalDate date;
    private LocalTime hour;
    private String reason;   // peut être null

    public Consultation() {
    }

    public Consultation(Integer id, Doctor doctor, Patient patient,
                        LocalDate date, LocalTime hour, String reason) {
        this.id = id;
        this.doctor = doctor;
        this.patient = patient;
        this.date = date;
        this.hour = hour;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", doctor=" + doctor +
                ", patient=" + patient +
                ", date=" + date +
                ", hour=" + hour +
                ", reason='" + reason + '\'' +
                '}';
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHour() { return hour; }
    public void setHour(LocalTime hour) { this.hour = hour; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
