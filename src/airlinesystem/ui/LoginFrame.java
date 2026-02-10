package airlinesystem.ui;

import airlinesystem.model.Passenger;
import airlinesystem.model.Staff;
import airlinesystem.repository.PassengerFileRepository;
import airlinesystem.repository.StaffFileRepository;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AppContext context;

    private JTextField passengerUsernameField;
    private JPasswordField passengerPasswordField;
    private JLabel passengerStatusLabel;

    private JTextField staffUsernameField;
    private JPasswordField staffPasswordField;
    private JLabel staffStatusLabel;

    public LoginFrame(AppContext context) {
        super("Airline Reservation System - Login");
        this.context = context;
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 320);                 // YÜKSEKLİĞİ ARTIRDIK (260 -> 320)
        setLocationRelativeTo(null);
        setResizable(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Passenger Login", createPassengerLoginPanel());
        tabbedPane.addTab("Staff Login", createStaffLoginPanel());

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JTextField createSizedTextField() {
        JTextField tf = new JTextField();
        Dimension d = new Dimension(180, 24);
        tf.setPreferredSize(d);
        tf.setMinimumSize(d);
        tf.setMaximumSize(d);
        return tf;
    }

    private JPasswordField createSizedPasswordField() {
        JPasswordField pf = new JPasswordField();
        Dimension d = new Dimension(180, 24);
        pf.setPreferredSize(d);
        pf.setMinimumSize(d);
        pf.setMaximumSize(d);
        return pf;
    }

    private JPanel createPassengerLoginPanel() {
        passengerUsernameField = createSizedTextField();
        passengerPasswordField = createSizedPasswordField();

        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(120, 30));
        loginButton.addActionListener(e -> handlePassengerLogin());

        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(120, 28));
        registerButton.addActionListener(e ->
                new RegisterPassengerFrame(context).setVisible(true)
        );

        passengerStatusLabel = new JLabel(" ");
        passengerStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        passengerStatusLabel.setForeground(new Color(0x555555));

        return buildFormPanel(
                "Passenger Login",
                passengerUsernameField,
                passengerPasswordField,
                loginButton,
                registerButton,
                passengerStatusLabel
        );
    }

    private JPanel createStaffLoginPanel() {
        staffUsernameField = createSizedTextField();
        staffPasswordField = createSizedPasswordField();

        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(120, 30));
        loginButton.addActionListener(e -> handleStaffLogin());

        staffStatusLabel = new JLabel(" ");
        staffStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        staffStatusLabel.setForeground(new Color(0x555555));

        return buildFormPanel(
                "Staff Login",
                staffUsernameField,
                staffPasswordField,
                loginButton,
                null,
                staffStatusLabel
        );
    }

    private JPanel buildFormPanel(String titleText,
                                  JTextField usernameField,
                                  JPasswordField passwordField,
                                  JButton loginButton,
                                  JButton registerButton,
                                  JLabel statusLabel) {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);

        int row = 0;

        JLabel title = new JLabel(titleText);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        c.gridx = 0;
        c.gridy = row++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(title, c);

        c.gridwidth = 1;
        c.gridy = row;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Username:"), c);

        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panel.add(usernameField, c);
        row++;

        c.gridy = row;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Password:"), c);

        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        panel.add(passwordField, c);
        row++;

        c.gridy = row++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, c);

        if (registerButton != null) {
            c.gridy = row++;
            c.gridx = 0;
            c.gridwidth = 2;
            panel.add(registerButton, c);
        }

        // login/register ile mesaj arasına boşluk
        c.gridy = row++;
        c.gridx = 0;
        c.gridwidth = 2;
        panel.add(Box.createVerticalStrut(10), c);

        c.gridy = row;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(4, 8, 0, 8);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        panel.add(statusLabel, c);

        return panel;
    }

    private void handlePassengerLogin() {
        String username = passengerUsernameField.getText().trim();
        String password = new String(passengerPasswordField.getPassword());

        PassengerFileRepository repo = context.getPassengerRepo();
        Passenger passenger = repo.login(username, password);

        if (passenger != null) {
            passengerStatusLabel.setText("Login successful: " + passenger.getFullName());
            SwingUtilities.invokeLater(() ->
                    new PassengerMainFrame(context, passenger).setVisible(true)
            );
            dispose();
        } else {
            passengerStatusLabel.setText("Login failed. Check username/password.");
        }
    }

    private void handleStaffLogin() {
        String username = staffUsernameField.getText().trim();
        String password = new String(staffPasswordField.getPassword());

        StaffFileRepository repo = context.getStaffRepo();
        Staff staff = repo.login(username, password);

        if (staff != null) {
            staffStatusLabel.setText("Login successful: " + staff.getFullName());
            SwingUtilities.invokeLater(() ->
                    new StaffMainFrame(context, staff).setVisible(true)
            );
            dispose();
        } else {
            staffStatusLabel.setText("Login failed. Check username/password.");
        }
    }
}
