package dao;

import entity.Consultation;
import entity.Doctor;
import entity.Patient;
import entity.Specialty;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.format.DateTimeFormatter;


public class ConsultationDAO {

    private ConnectDB connectDB;
    private ArrayList<Consultation> consultations;

    public ConsultationDAO() {
        connectDB = new ConnectDB();
        consultations = new ArrayList<>();
    }

    public ArrayList<Consultation> getList() {
        return consultations;
    }

    public Consultation getById(Integer id) {
        for (Consultation c : consultations) {
            if (Objects.equals(c.getId(), id)) {
                return c;
            }
        }
        return null;
    }

    // --- READ : charger toutes les consultations ---
    public ArrayList<Consultation> load() {
        try {
            String sql = """
                    SELECT c.id          AS consultation_id,
                           c.date        AS consultation_date,
                           c.hour        AS consultation_hour,
                           c.reason      AS consultation_reason,
                           d.id          AS doctor_id,
                           d.last_name   AS doctor_last_name,
                           d.first_name  AS doctor_first_name,
                           s.id          AS specialty_id,
                           s.name        AS specialty_name,
                           p.id          AS patient_id,
                           p.last_name   AS patient_last_name,
                           p.first_name  AS patient_first_name,
                           p.birth_date  AS patient_birth_date
                    FROM consultations c
                    JOIN doctors d      ON c.doctor_id  = d.id
                    LEFT JOIN specialties s ON d.specialty_id = s.id
                    LEFT JOIN patients p    ON c.patient_id   = p.id
                    ORDER BY c.date, c.hour;
                    """;

            PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            consultations.clear();

            while (rs.next()) {
                Integer consId = rs.getInt("consultation_id");
                java.sql.Date sqlDate = rs.getDate("consultation_date");
                LocalDate date = null;
                if (sqlDate != null) {
                    date = sqlDate.toLocalDate();
                }

                String hourStr = rs.getString("consultation_hour");
                LocalTime hour = null;
                if (hourStr != null && !hourStr.isBlank()) {
                    hour = LocalTime.parse(hourStr);
                }


                String reason = rs.getString("consultation_reason");

                // Doctor + Specialty
                Integer doctorId = rs.getInt("doctor_id");
                String doctorLastName = rs.getString("doctor_last_name");
                String doctorFirstName = rs.getString("doctor_first_name");

                Integer specId = rs.getInt("specialty_id");
                Specialty spec = null;
                if (!rs.wasNull()) {
                    String specName = rs.getString("specialty_name");
                    spec = new Specialty(specId, specName);
                }

                Doctor doctor = new Doctor(doctorId, spec, doctorLastName, doctorFirstName);

                // Patient
                Integer patientId = rs.getInt("patient_id");
                Patient patient = null;
                if (!rs.wasNull()) {
                    String patientLastName = rs.getString("patient_last_name");
                    String patientFirstName = rs.getString("patient_first_name");
                    java.sql.Date sqlBirth = rs.getDate("patient_birth_date");
                    LocalDate birthDate = null;
                    if (sqlBirth != null) {
                        birthDate = sqlBirth.toLocalDate();
                    }
                    patient = new Patient(patientId, patientLastName, patientFirstName, birthDate);
                }

                Consultation c = new Consultation(consId, doctor, patient, date, hour, reason);
                consultations.add(c);
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConsultationDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return consultations;
        }
    }

    // --- C + U : CREATE / UPDATE ---
    public void save(Consultation c) {
        if (c == null) return;

        try {
            Integer doctorId = null;
            Integer patientId = null;

            if (c.getDoctor() != null) {
                doctorId = c.getDoctor().getId();
            }
            if (c.getPatient() != null) {
                patientId = c.getPatient().getId();
            }

            if (c.getId() != null) {
                // UPDATE
                String sql = """
                        UPDATE consultations
                        SET doctor_id = ?, patient_id = ?, date = ?, hour = ?, reason = ?
                        WHERE id = ?
                        """;
                PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);

                // doctor_id
                if (doctorId != null) {
                    stmt.setInt(1, doctorId);
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }

                // patient_id
                if (patientId != null) {
                    stmt.setInt(2, patientId);
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }

                // date
                if (c.getDate() != null) {
                    stmt.setDate(3, java.sql.Date.valueOf(c.getDate()));
                } else {
                    stmt.setNull(3, Types.DATE);
                }

                // hour
                if (c.getHour() != null) {
                    stmt.setTime(4, Time.valueOf(c.getHour()));
                } else {
                    stmt.setNull(4, Types.TIME);
                }

                stmt.setString(5, c.getReason());
                stmt.setInt(6, c.getId());

                stmt.executeUpdate();
                stmt.close();
            } else {
                // INSERT
                String sql = """
                        INSERT INTO consultations (doctor_id, patient_id, date, hour, reason)
                        VALUES (?, ?, ?, ?, ?)
                        """;
                PreparedStatement stmt = connectDB.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                // doctor_id
                if (doctorId != null) {
                    stmt.setInt(1, doctorId);
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }

                // patient_id
                if (patientId != null) {
                    stmt.setInt(2, patientId);
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }

                // date
                if (c.getDate() != null) {
                    stmt.setDate(3, java.sql.Date.valueOf(c.getDate()));
                } else {
                    stmt.setNull(3, Types.DATE);
                }

                // hour
                if (c.getHour() != null) {
                    stmt.setTime(4, Time.valueOf(c.getHour()));
                } else {
                    stmt.setNull(4, Types.TIME);
                }

                stmt.setString(5, c.getReason());

                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    c.setId((int) rs.getLong(1));
                }
                rs.close();
                stmt.close();

                consultations.add(c);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConsultationDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // --- D : DELETE ---
    public void delete(Consultation c) {
        if (c != null && c.getId() != null) {
            delete(c.getId());
        }
    }

    public void delete(Integer id) {
        if (id == null) return;

        try {
            String sql = "DELETE FROM consultations WHERE id = ?";
            PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);

            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt.close();

            consultations.removeIf(cons -> Objects.equals(cons.getId(), id));
        } catch (SQLException ex) {
            Logger.getLogger(ConsultationDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
