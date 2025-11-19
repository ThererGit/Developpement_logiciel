package entity;

public class Doctor {
    private Integer id;
    private Specialty specialty;
    private String lastName;
    private String firstName;
    private String login;
    private String password;

    public Doctor() {
    }

    // ancien constructeur conservé pour compatibilité
    public Doctor(Integer id, Specialty specialty, String lastName, String firstName) {
        this(id, specialty, lastName, firstName, null, null);
    }

    // nouveau constructeur complet
    public Doctor(Integer id,
                  Specialty specialty,
                  String lastName,
                  String firstName,
                  String login,
                  String password) {
        this.id = id;
        this.specialty = specialty;
        this.lastName = lastName;
        this.firstName = firstName;
        this.login = login;
        this.password = password;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", specialty=" + specialty +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", login='" + login + '\'' +
                // on NE met PAS le password dans le toString (sécurité)
                '}';
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Specialty getSpecialty() { return specialty; }
    public void setSpecialty( Specialty specialty) { this.specialty = specialty; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
