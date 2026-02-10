package airlinesystem.ui;

import airlinesystem.repository.PassengerFileRepository;

import javax.swing.*;
import java.awt.*;

public class RegisterPassengerFrame extends JFrame {

    private final AppContext context;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private JTextField surnameField;
    private JTextField contactField;
    private JTextField passportField;
    private JLabel statusLabel;

    public RegisterPassengerFrame(AppContext context) {
        super("Passenger Registration");
        this.context = context;
        initUI();
    }

    private void initUI() {
        setSize(450, 420);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.anchor = GridBagConstraints.WEST;

        int y = 0;

        JLabel title = new JLabel("Create Passenger Account");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(title, c);

        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = y;
        panel.add(new JLabel("Username:"), c);
        usernameField = new JTextField(16);
        c.gridx = 1;
        panel.add(usernameField, c);
        y++;

        c.gridx = 0; c.gridy = y;
        panel.add(new JLabel("Password:"), c);
        passwordField = new JPasswordField(16);
        c.gridx = 1;
        panel.add(passwordField, c);
        y++;

        c.gridx = 0; c.gridy = y;
        panel.add(new JLabel("Name:"), c);
        nameField = new JTextField(16);
        c.gridx = 1;
        panel.add(nameField, c);
        y++;

        c.gridx = 0; c.gridy = y;
        panel.add(new JLabel("Surname:"), c);
        surnameField = new JTextField(16);
        c.gridx = 1;
        panel.add(surnameField, c);
        y++;

        c.gridx = 0; c.gridy = y;
        panel.add(new JLabel("Contact Info:"), c);
        contactField = new JTextField(16);
        c.gridx = 1;
        panel.add(contactField, c);
        y++;

        c.gridx = 0; c.gridy = y;
        panel.add(new JLabel("Passport No:"), c);
        passportField = new JTextField(16);
        c.gridx = 1;
        panel.add(passportField, c);
        y++;

        // CREATE ACCOUNT BUTTON
        JButton registerBtn = new JButton("Create Account");
        registerBtn.addActionListener(e -> handleRegister());
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(registerBtn, c);

        // CLOSE BUTTON
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> {
            this.dispose();
        });
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(closeBtn, c);

        // STATUS LABEL
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(statusLabel, c);

        add(panel);
    }

    private void handleRegister() {
        try {
            PassengerFileRepository repo = context.getPassengerRepo();

            // Alanları önce değişkenlere al
            String username    = usernameField.getText().trim();
            String password    = new String(passwordField.getPassword()).trim();
            String name        = nameField.getText().trim();
            String surname     = surnameField.getText().trim();
            String contactInfo = contactField.getText().trim();
            String passportNo  = passportField.getText().trim();

            // -------------------------
            // 1) Boş alan kontrolü
            // -------------------------
            if (username.isEmpty() || password.isEmpty() ||
                name.isEmpty() || surname.isEmpty() ||
                contactInfo.isEmpty() || passportNo.isEmpty()) {

                statusLabel.setText("All fields are required.");
                return;
            }

            // -------------------------
            // 2) Username min 8 karakter
            // -------------------------
            if (username.length() < 8) {
                statusLabel.setText("Username must be at least 8 characters.");
                return;
            }

            // -------------------------
            // 3) Password en az 6 karakter
            // -------------------------
            if (password.length() < 6) {
                statusLabel.setText("Password must be at least 6 characters.");
                return;
            }

            // -------------------------
            // 4) Contact Info gmail zorunlu
            // -------------------------
            if (!contactInfo.contains("@gmail.com")) {
                statusLabel.setText("Contact info must contain '@gmail.com'.");
                return;
            }

            // -------------------------
            // 5) Pasaport no tam 9 karakter
            // -------------------------
            if (passportNo.length() != 9) {
                statusLabel.setText("Passport number must be exactly 9 characters.");
                return;
            }

            // if (!passportNo.matches("\\d{9}")) {
            //     statusLabel.setText("Passport number must contain only digits (9 digits).");
            //     return;
            // }

            // -------------------------
            // 6) Kayıt oluşturma
            // -------------------------
            String newId = repo.generateNewPassengerId();

            repo.register(
                    username,
                    password,
                    newId,
                    name,
                    surname,
                    contactInfo,
                    passportNo
            );

            repo.saveAll();
            statusLabel.setText("Registration successful. Your ID: " + newId);

        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

}
