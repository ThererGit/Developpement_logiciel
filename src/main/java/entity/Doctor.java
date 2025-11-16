package entity;


public class Doctor {
    private Integer id;
    private Specialty specialty;
    private String lastName;
    private String firstName;

    public Doctor() {
    }

    public Doctor(Integer id, Specialty specialty, String lastName, String firstName) {
        this.id = id;
        this.specialty = specialty;
        this.lastName = lastName;
        this.firstName = firstName;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", specialty=" + specialty +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                '}';
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Specialty getSpecialty() { return specialty; }
    public void setSpecialty(Specialty specialty) { this.specialty = specialty; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
}
