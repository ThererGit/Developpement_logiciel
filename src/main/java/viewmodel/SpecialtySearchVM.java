package viewmodel;

import entity.Specialty;

public class SpecialtySearchVM {

    private Specialty specialty;

    private Integer id;
    private String name;

    public SpecialtySearchVM(Specialty specialty) {
        this.specialty = specialty;
        if (specialty != null) {
            this.id = specialty.getId();
            this.name = specialty.getName();
        }
    }

    // Accès à l'entité complète
    public Specialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
        if (specialty != null) {
            this.id = specialty.getId();
            this.name = specialty.getName();
        }
    }

    // Champs utilisés par la vue (TableView, listes, etc.)

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name != null ? name : ("Specialty #" + id);
    }
}
