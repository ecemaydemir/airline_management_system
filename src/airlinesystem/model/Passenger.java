package airlinesystem.model;

public class Passenger extends Person {

    private String passportNum;

    // Login için eklenen alanlar
    private String username;
    private String password;

    /**
     * Kayıtlı user/passenger oluştururken kullanılır (Login gerektiren sistemlerde).
     */
    public Passenger(String passengerID,
                     String name,
                     String surname,
                     String contactInfo,
                     String passportNum,
                     String username,
                     String password) {

        super(passengerID, name, surname, contactInfo);
        setPassportNum(passportNum);
        setUsername(username);
        setPassword(password);
    }

    public Passenger(String passengerID,
                     String name,
                     String surname,
                     String contactInfo,
                     String passportNum) {

        super(passengerID, name, surname, contactInfo);
        setPassportNum(passportNum);

        // Login kullanılmıyorsa null olabilir
        this.username = null;
        this.password = null;
    }

    public String getPassengerID() {
        return getPersonID();
    }

    public String getPassportNum() {
        return passportNum;
    }

    public void setPassportNum(String passportNum) {
        if (passportNum == null || passportNum.isBlank()) {
            throw new IllegalArgumentException("Passport number cannot be null or blank");
        }
        this.passportNum = passportNum;
    }

    // ========== LOGIN FIELDS ==========

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        this.password = password;
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "fullName=" + getFullName() +
                ", id=" + getPersonID() +
                ", passport='" + passportNum + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
