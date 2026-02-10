package airlinesystem.model;

public class Staff extends Person {

    private final String username;
    private String password;
    private String position;  // STAFF veya ADMIN

    public Staff(String staffID,
                 String name,
                 String surname,
                 String contactInfo,
                 String username,
                 String password,
                 String position) {

        super(staffID, name, surname, contactInfo);

        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be empty");

        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password cannot be empty");

        if (position == null || position.isBlank())
            throw new IllegalArgumentException("Position cannot be empty");

        this.username = username;
        this.password = password;
        this.position = position.toUpperCase();
    }

    public String getStaffID() {
        return getPersonID();
    }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public void setPassword(String password) {
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password cannot be empty");
        this.password = password;
    }

    public String getPosition() { return position; }

    public void setPosition(String position) {
        if (position == null || position.isBlank())
            throw new IllegalArgumentException("Position cannot be empty");
        this.position = position.toUpperCase();
    }

    public boolean isAdmin() {
        return "ADMIN".equals(position);
    }

    @Override
    public String toString() {
        return "Staff{" +
                "fullName='" + getFullName() + '\'' +
                ", username='" + username + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}
