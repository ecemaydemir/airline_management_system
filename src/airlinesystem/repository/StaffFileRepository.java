package airlinesystem.repository;

import airlinesystem.model.Staff;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StaffFileRepository {

    private final Path filePath;

    // username -> staff
    private final Map<String, Staff> staffByUsername = new HashMap<>();
    // staffID -> staff
    private final Map<String, Staff> staffById = new HashMap<>();

    public StaffFileRepository(String fileName) {
        this.filePath = Path.of(fileName);
    }

    // ----------------------------------------------------
    // LOAD / SAVE
    // ----------------------------------------------------

    public void loadAll() throws IOException {
        staffByUsername.clear();
        staffById.clear();

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

                String staffID     = parts[0];
                String name        = parts[1];
                String surname     = parts[2];
                String contactInfo = parts[3];
                String username    = parts[4];
                String password    = parts[5];
                String position    = parts[6];

                Staff staff = new Staff(
                        staffID,
                        name,
                        surname,
                        contactInfo,
                        username,
                        password,
                        position
                );

                staffByUsername.put(username, staff);
                staffById.put(staffID, staff);
            }
        }
    }

    public void saveAll() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            for (Staff s : staffByUsername.values()) {
                String line = String.join(";",
                        s.getPersonID(),
                        s.getName(),
                        s.getSurname(),
                        s.getContactInfo(),
                        s.getUsername(),
                        s.getPassword(),
                        s.getPosition()
                );
                bw.write(line);
                bw.newLine();
            }
        }
    }

    // ----------------------------------------------------
    // QUERY / LOGIN
    // ----------------------------------------------------

    public Staff findById(String staffID) {
        if (staffID == null) return null;
        return staffById.get(staffID);
    }

    public Staff findByUsername(String username) {
        if (username == null) return null;
        return staffByUsername.get(username);
    }

    public Staff login(String username, String password) {
        if (username == null || password == null) return null;
        Staff s = staffByUsername.get(username);
        if (s == null) return null;
        return password.equals(s.getPassword()) ? s : null;
    }

    public Collection<Staff> getAllStaff() {
        return staffByUsername.values();
    }

    // ----------------------------------------------------
    // ADD NEW STAFF
    // ----------------------------------------------------

    public void addStaff(Staff staff) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }
        if (staffByUsername.containsKey(staff.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + staff.getUsername());
        }
        if (staffById.containsKey(staff.getPersonID())) {
            throw new IllegalArgumentException("Staff ID already exists: " + staff.getPersonID());
        }

        staffByUsername.put(staff.getUsername(), staff);
        staffById.put(staff.getPersonID(), staff);
    }

    // ----------------------------------------------------
    // UPDATE STAFF (username + staffID DEĞİŞMEZ)
    // ----------------------------------------------------

    public void updateStaff(Staff updated) {
        if (updated == null) {
            throw new IllegalArgumentException("Updated staff cannot be null");
        }

        Staff existing = staffByUsername.get(updated.getUsername());
        if (existing == null) {
            throw new IllegalArgumentException("Staff not found: " + updated.getUsername());
        }

        existing.setName(updated.getName());
        existing.setSurname(updated.getSurname());
        existing.setContactInfo(updated.getContactInfo());
        existing.setPassword(updated.getPassword());
        existing.setPosition(updated.getPosition());
    }

    // ----------------------------------------------------
    // DELETE STAFF
    // ----------------------------------------------------

    public boolean deleteStaff(String username) {
        Staff s = staffByUsername.remove(username);
        if (s == null) {
            return false;
        }
        staffById.remove(s.getPersonID());
        return true;
    }

    // ----------------------------------------------------
    // AUTO STAFF ID GENERATION  (S001, S002, ...)
    // ----------------------------------------------------

    /**
     * Dosyadaki mevcut staff ID'lerine bakıp S001, S002, ... şeklinde
     * yeni bir staff ID üretir. Sadece 'S' veya 's' ile başlayan ID'leri dikkate alır.
     */
    public String generateNewStaffId() {
        int max = 0;

        if (!Files.exists(filePath)) {
            return "S001";
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length < 1) {
                    continue;
                }

                // staffID;name;surname;contact;username;password;position
                String id = parts[0];
                if (id != null && id.length() > 1 &&
                        (id.charAt(0) == 'S' || id.charAt(0) == 's')) {
                    try {
                        int n = Integer.parseInt(id.substring(1));
                        if (n > max) {
                            max = n;
                        }
                    } catch (NumberFormatException ignore) {
                        // SABC gibi saçma ID varsa yok say
                    }
                }
            }
        } catch (IOException e) {
            // IO hatası olursa patlatma, sıfırdan başla
            return "S001";
        }

        int next = max + 1;
        return String.format("S%03d", next);   // S001, S002, ...
    }
}
