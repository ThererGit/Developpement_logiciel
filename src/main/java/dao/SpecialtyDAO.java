package dao;

import entity.Specialty;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpecialtyDAO {

    private ConnectDB connectDB;
    private ArrayList<Specialty> specialties;

    public SpecialtyDAO() {
        connectDB = new ConnectDB();
        specialties = new ArrayList<>();
    }

    public ArrayList<Specialty> getList() {
        return specialties;
    }

    public Specialty getById(Integer id) {
        for (Specialty s : specialties) {
            if (Objects.equals(s.getId(), id)) {
                return s;
            }
        }
        return null;
    }

    // --- READ : charger toutes les spécialités ---
    public ArrayList<Specialty> load() {
        try {
            String sql = "SELECT id, name FROM specialties ORDER BY name;";
            PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();
            specialties.clear();

            while (rs.next()) {
                Integer id = rs.getInt("id");
                String name = rs.getString("name");

                Specialty s = new Specialty(id, name);
                specialties.add(s);
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(SpecialtyDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return specialties;
        }
    }

    // --- C + U : CREATE / UPDATE ---
    public void save(Specialty s) {
        if (s == null) return;

        try {
            if (s.getId() != null) {
                // UPDATE
                String sql = "UPDATE specialties SET name = ? WHERE id = ?";
                PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);

                stmt.setString(1, s.getName());
                stmt.setInt(2, s.getId());

                stmt.executeUpdate();
                stmt.close();
            } else {
                // INSERT
                String sql = "INSERT INTO specialties (name) VALUES (?)";
                PreparedStatement stmt = connectDB.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, s.getName());

                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    s.setId((int) rs.getLong(1));
                }
                rs.close();
                stmt.close();

                // on peut aussi l'ajouter à la liste en mémoire
                specialties.add(s);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SpecialtyDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // --- D : DELETE ---
    public void delete(Specialty s) {
        if (s != null && s.getId() != null) {
            delete(s.getId());
        }
    }

    public void delete(Integer id) {
        if (id == null) return;

        try {
            String sql = "DELETE FROM specialties WHERE id = ?";
            PreparedStatement stmt = connectDB.getConn().prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt.close();

            specialties.removeIf(sp -> Objects.equals(sp.getId(), id));
        } catch (SQLException ex) {
            Logger.getLogger(SpecialtyDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
