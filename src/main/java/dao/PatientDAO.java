package dao;

import entity.Patient;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.*;


public class PatientDAO {

    private ConnectDB connectDB;
    private ArrayList<Patient> patients;

    public PatientDAO() {
        connectDB = new ConnectDB();   // encapsule la connexion
        patients = new ArrayList<>();  // cache mémoire des patients
    }

    public ArrayList<Patient> getList() {
        return patients;
    }

    public Patient getById(Integer id) {
        for (Patient p : patients) {
            if (Objects.equals(p.getId(), id)) {
                return p;
            }
        }
        return null;
    }

    // --- READ : charger tous les patients ---
    public ArrayList<Patient> load() {
        try {
            String sql = "SELECT id, last_name, first_name, birth_date FROM patients ORDER BY last_name;";
            PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();
            patients.clear();

            while (rs.next()) {
                Integer id = rs.getInt("id");
                String lastName = rs.getString("last_name");
                String firstName = rs.getString("first_name");

                LocalDate birthDate = null;
                java.sql.Date sqlDate = rs.getDate("birth_date");  // ← bien java.sql.Date
                if (sqlDate != null) {
                    birthDate = sqlDate.toLocalDate();             // ← maintenant ça compile
                }

                Patient p = new Patient(id, lastName, firstName, birthDate);
                patients.add(p);
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(PatientDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return patients;
        }
    }


    // --- C + U : CREATE / UPDATE ---
    public void save(Patient p) {
        if (p == null) return;

        try {
            if (p.getId() != null) {
                // UPDATE
                String sql = "UPDATE patients SET last_name = ?, first_name = ?, birth_date = ? WHERE id = ?";
                PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);

                stmt.setString(1, p.getLastName());
                stmt.setString(2, p.getFirstName());
                if (p.getBirthDate() != null) {
                    stmt.setDate(3, java.sql.Date.valueOf(p.getBirthDate()));
                } else {
                    stmt.setNull(3, Types.DATE);
                }
                stmt.setInt(4, p.getId());

                stmt.executeUpdate();
                stmt.close();
            } else {
                // INSERT
                String sql = "INSERT INTO patients (last_name, first_name, birth_date) VALUES (?, ?, ?)";
                PreparedStatement stmt = connectDB.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, p.getLastName());
                stmt.setString(2, p.getFirstName());
                if (p.getBirthDate() != null) {
                    stmt.setDate(3, java.sql.Date.valueOf(p.getBirthDate()));
                } else {
                    stmt.setNull(3, Types.DATE);
                }

                stmt.executeUpdate();

                // récupérer l'id auto-généré
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    p.setId((int) rs.getLong(1));
                }
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PatientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // --- D : DELETE ---
    public void delete(Patient p) {
        if (p != null && p.getId() != null) {
            delete(p.getId());
        }
    }

    public void delete(Integer id) {
        if (id == null) return;

        try {
            String sql = "DELETE FROM patients WHERE id = ?";
            PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(PatientDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Patient findByNameAndFirstName(String nom, String prenom) {
        try {
            String sql = "SELECT * FROM patients WHERE last_name = ? AND first_name = ? LIMIT 1";
            PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);
            stmt.setString(1, nom);
            stmt.setString(2, prenom);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Integer id = rs.getInt("id");
                LocalDate birth = rs.getDate("birth_date") != null
                        ? rs.getDate("birth_date").toLocalDate()
                        : null;

                return new Patient(id, nom, prenom, birth);
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


}
