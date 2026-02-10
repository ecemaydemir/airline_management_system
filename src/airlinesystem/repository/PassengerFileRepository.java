package airlinesystem.repository;

import airlinesystem.model.Passenger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PassengerFileRepository {

    private final Path filePath;

    // username -> Passenger
    private final Map<String, Passenger> passengersByUsername = new HashMap<>();
    // passengerID -> Passenger
    private final Map<String, Passenger> passengersById = new HashMap<>();

    public PassengerFileRepository(String fileName) {
        this.filePath = Path.of(fileName);
    }

    // ---------- LOAD / SAVE ----------

    public void loadAll() throws IOException {
        passengersByUsername.clear();
        passengersById.clear();

        if (!Files.exists(filePath)) {
            return;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length != 7) {
                    continue;
                }

                String username    = parts[0];
                String password    = parts[1];
                String passengerID = parts[2];
                String name        = parts[3];
                String surname     = parts[4];
                String contactInfo = parts[5];
                String passportNum = parts[6];

                Passenger p = new Passenger(
                        passengerID,
                        name,
                        surname,
                        contactInfo,
                        passportNum,
                        username,
                        password
                );

                passengersByUsername.put(username, p);
                passengersById.put(passengerID, p);
            }
        }
    }

    public void saveAll() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            for (Passenger p : passengersByUsername.values()) {
                String line = String.join(";",
                        p.getUsername(),
                        p.getPassword(),
                        p.getPassengerID(),
                        p.getName(),
                        p.getSurname(),
                        p.getContactInfo(),
                        p.getPassportNum()
                );
                bw.write(line);
                bw.newLine();
            }
        }
    }

    // ---------- EXISTENCE / DUPLICATE CHECKS ----------

    public boolean usernameExists(String username) {
        if (username == null) return false;
        // Case-insensitive kontrol
        return passengersByUsername.keySet().stream()
                .anyMatch(u -> u.equalsIgnoreCase(username));
    }

    public boolean contactExists(String contactInfo) {
        if (contactInfo == null) return false;
        return passengersByUsername.values().stream()
                .anyMatch(p -> p.getContactInfo() != null &&
                               p.getContactInfo().equalsIgnoreCase(contactInfo));
    }

    public boolean passportExists(String passportNum) {
        if (passportNum == null) return false;
        return passengersByUsername.values().stream()
                .anyMatch(p -> p.getPassportNum() != null &&
                               p.getPassportNum().equalsIgnoreCase(passportNum));
    }

    // ---------- QUERY / LOGIN / REGISTER ----------

    public Passenger findById(String passengerID) {
        if (passengerID == null) return null;
        return passengersById.get(passengerID);
    }

    public Passenger findByUsername(String username) {
        if (username == null) return null;
        return passengersByUsername.get(username);
    }

    public Passenger login(String username, String password) {
        if (username == null || password == null) return null;
        Passenger p = passengersByUsername.get(username);
        if (p == null) return null;
        return password.equals(p.getPassword()) ? p : null;
    }

    public Passenger register(String username,
                              String password,
                              String passengerID,
                              String name,
                              String surname,
                              String contactInfo,
                              String passportNum) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username/password cannot be blank");
        }

        // Username kontrolü (case-insensitive)
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        // ID kontrolü
        if (passengersById.containsKey(passengerID)) {
            throw new IllegalArgumentException("PassengerID already exists: " + passengerID);
        }

        // Contact info kontrolü
        if (contactExists(contactInfo)) {
            throw new IllegalArgumentException("Contact info already in use: " + contactInfo);
        }

        // Passport numarası kontrolü
        if (passportExists(passportNum)) {
            throw new IllegalArgumentException("Passport number already in use: " + passportNum);
        }

        Passenger p = new Passenger(
                passengerID,
                name,
                surname,
                contactInfo,
                passportNum,
                username,
                password
        );

        passengersByUsername.put(username, p);
        passengersById.put(passengerID, p);

        return p;
    }

    public String generateNewPassengerId() {
        int max = 0;

        if (!Files.exists(filePath)) {
            return "P001";
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(";");
                if (parts.length < 3) continue;

                String id = parts[2];          // username;password;ID;...
                if (id != null && id.length() > 1 &&
                        (id.charAt(0) == 'P' || id.charAt(0) == 'p')) {
                    try {
                        int n = Integer.parseInt(id.substring(1));
                        if (n > max) max = n;
                    } catch (NumberFormatException ignore) { }
                }
            }
        } catch (IOException e) {
            return "P001";
        }

        int next = max + 1;
        return String.format("P%03d", next);   // P001, P002, ...
    }

    public Collection<Passenger> getAllPassengers() {
        return passengersByUsername.values();
    }
}
