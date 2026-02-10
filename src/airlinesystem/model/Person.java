package airlinesystem.model;

import java.util.Objects;

public abstract class Person {

    private final String personID;
    private String name;
    private String surname;
    private String contactInfo;

    protected Person(String personID, String name, String surname, String contactInfo) {
        if (personID == null || personID.isBlank()) {
            throw new IllegalArgumentException("Person ID cannot be null or blank");
        }
        this.personID = personID;
        this.setName(name);
        this.setSurname(surname);
        this.setContactInfo(contactInfo);
    }

    public String getPersonID() {
        return personID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        if (surname == null || surname.isBlank()) {
            throw new IllegalArgumentException("Surname cannot be null or blank");
        }
        this.surname = surname;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        if (contactInfo == null || contactInfo.isBlank()) {
            throw new IllegalArgumentException("Contact info cannot be null or blank");
        }
        this.contactInfo = contactInfo;
    }

    public String getFullName() {
        return name + " " + surname;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + personID + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return personID.equals(person.personID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personID);
    }
}
