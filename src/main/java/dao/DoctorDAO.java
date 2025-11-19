package dao;

import entity.Doctor;
import entity.Specialty;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoctorDAO {

    private ConnectDB connectDB;
    private ArrayList<Doctor> doctors;

    public DoctorDAO() {
        connectDB = new ConnectDB();
        doctors = new ArrayList<>();
    }

    public ArrayList<Doctor> getList() {
        return doctors;
    }

    public Doctor getById(Integer id) {
        for (Doctor d : doctors) {
            if (Objects.equals(d.getId(), id)) {
                return d;
            }
        }
        return null;
    }

    // --- READ : charger tous les docteurs ---
    public ArrayList<Doctor> load() {
        try {
            // On récupère aussi login et password
            String sql = """
                    SELECT d.id        AS doctor_id,
                           d.last_name,
                           d.first_name,
                           d.login,
                           d.password,
                           s.id        AS specialty_id,
                           s.name      AS specialty_name
                    FROM doctors d
                    LEFT JOIN specialties s ON d.specialty_id = s.id
                    ORDER BY d.last_name;
                    """;

            PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            doctors.clear();

            while (rs.next()) {
                Integer docId = rs.getInt("doctor_id");
                String lastName = rs.getString("last_name");
                String firstName = rs.getString("first_name");
                String login = rs.getString("login");
                String password = rs.getString("password");

                Integer specId = rs.getInt("specialty_id");
                Specialty spec = null;
                if (!rs.wasNull()) {
                    String specName = rs.getString("specialty_name");
                    spec = new Specialty(specId, specName);
                }

                Doctor d = new Doctor(docId, spec, lastName, firstName, login, password);
                doctors.add(d);
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(DoctorDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return doctors;
        }
    }

    // --- C + U : CREATE / UPDATE ---
    public void save(Doctor d) {
        if (d == null) return;

        try {
            Integer specId = null;
            if (d.getSpecialty() != null) {
                specId = d.getSpecialty().getId();
            }

            if (d.getId() != null) {
                // UPDATE
                String sql = """
                        UPDATE doctors
                        SET last_name   = ?,
                            first_name  = ?,
                            specialty_id = ?,
                            login       = ?,
                            password    = ?
                        WHERE id = ?
                        """;
                PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);

                stmt.setString(1, d.getLastName());
                stmt.setString(2, d.getFirstName());
                if (specId != null) {
                    stmt.setInt(3, specId);
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }
                stmt.setString(4, d.getLogin());
                stmt.setString(5, d.getPassword());
                stmt.setInt(6, d.getId());

                stmt.executeUpdate();
                stmt.close();
            } else {
                // INSERT
                String sql = """
                        INSERT INTO doctors (last_name, first_name, specialty_id, login, password)
                        VALUES (?, ?, ?, ?, ?)
                        """;
                PreparedStatement stmt = connectDB.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, d.getLastName());
                stmt.setString(2, d.getFirstName());
                if (specId != null) {
                    stmt.setInt(3, specId);
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }
                stmt.setString(4, d.getLogin());
                stmt.setString(5, d.getPassword());

                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    d.setId((int) rs.getLong(1));
                }
                rs.close();
                stmt.close();

                doctors.add(d);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DoctorDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // --- D : DELETE ---
    public void delete(Doctor d) {
        if (d != null && d.getId() != null) {
            delete(d.getId());
        }
    }

    public void delete(Integer id) {
        if (id == null) return;

        try {
            String sql = "DELETE FROM doctors WHERE id = ?";
            PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);

            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt.close();

            doctors.removeIf(doc -> Objects.equals(doc.getId(), id));
        } catch (SQLException ex) {
            Logger.getLogger(DoctorDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Doctor checkLogin(String login, String password) {
        try {
            String sql = """
                SELECT d.id, d.last_name, d.first_name, d.login, d.password,
                       s.id AS specialty_id, s.name AS specialty_name
                FROM doctors d
                LEFT JOIN specialties s ON d.specialty_id = s.id
                WHERE d.login = ? AND d.password = ?
                """;

            PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);
            stmt.setString(1, login);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Integer id = rs.getInt("id");
                String lastName = rs.getString("last_name");
                String firstName = rs.getString("first_name");

                Integer specId = rs.getInt("specialty_id");
                Specialty spec = null;
                if (!rs.wasNull()) {
                    spec = new Specialty(specId, rs.getString("specialty_name"));
                }

                rs.close();
                stmt.close();

                return new Doctor(id, spec, lastName, firstName, login, null);
            }

            rs.close();
            stmt.close();

        } catch (Exception ex) {
            Logger.getLogger(DoctorDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        // login/password incorrects
        return null;
    }



}
