package airlinesystem.ui;

import airlinesystem.model.Staff;
import airlinesystem.repository.StaffFileRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StaffManagementDialog extends JDialog {

    private final StaffFileRepository staffRepo;

    private JTable staffTable;
    private DefaultTableModel staffTableModel;
    private JLabel statusLabel;

    public StaffManagementDialog(Frame parent, StaffFileRepository staffRepo) {
        super(parent, "Manage Staff Information", true);
        this.staffRepo = staffRepo;

        initUI();
        loadStaffToTable();
    }

    // ----------------------------------------------------
    // UI
    // ----------------------------------------------------

    private void initUI() {
        setSize(800, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.EAST);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane createTablePanel() {
        String[] cols = {
                "Staff ID",
                "Name",
                "Surname",
                "Contact",
                "Username",
                "Position"
        };

        staffTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        staffTable = new JTable(staffTableModel);
        staffTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        return new JScrollPane(staffTable);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addBtn = new JButton("Add Staff");
        addBtn.addActionListener(e -> doAddStaff());
        panel.add(addBtn);

        JButton editBtn = new JButton("Edit Selected");
        editBtn.addActionListener(e -> doEditStaff());
        panel.add(editBtn);

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> doDeleteStaff());
        panel.add(deleteBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        panel.add(closeBtn);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    // ----------------------------------------------------
    // TABLE LOAD
    // ----------------------------------------------------

    private void loadStaffToTable() {
        staffTableModel.setRowCount(0);

        List<Staff> staffList = new ArrayList<>(staffRepo.getAllStaff());
        staffList.sort(Comparator.comparing(Staff::getUsername));

        for (Staff s : staffList) {
            staffTableModel.addRow(new Object[]{
                    s.getPersonID(),
                    s.getName(),
                    s.getSurname(),
                    s.getContactInfo(),
                    s.getUsername(),
                    s.getPosition()
            });
        }

        statusLabel.setText("Total staff: " + staffList.size());
    }

    // ----------------------------------------------------
    // ADD STAFF
    // ----------------------------------------------------

    private void doAddStaff() {
        // Add modunda initialData = null
        StaffFormData data = showStaffForm(null);
        if (data == null) {
            return; // Cancel
        }

        try {
            // ID'yi kullanıcıdan almıyoruz, repo otomatik üretiyor
            String newStaffId = staffRepo.generateNewStaffId();

            Staff newStaff = new Staff(
                    newStaffId,
                    data.name,
                    data.surname,
                    data.contact,
                    data.username,
                    data.password,
                    data.position
            );

            staffRepo.addStaff(newStaff);
            staffRepo.saveAll();
            loadStaffToTable();
            statusLabel.setText("Staff added: " + newStaff.getUsername() + " (ID: " + newStaffId + ")");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error adding staff: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException ioEx) {
            JOptionPane.showMessageDialog(this,
                    "Error saving staff file: " + ioEx.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----------------------------------------------------
    // EDIT STAFF
    // ----------------------------------------------------

    private void doEditStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a staff to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) staffTableModel.getValueAt(row, 4);
        Staff existing = staffRepo.findByUsername(username);
        if (existing == null) {
            JOptionPane.showMessageDialog(this,
                    "Selected staff not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        StaffFormData initial = new StaffFormData();
        initial.staffID = existing.getPersonID();
        initial.name = existing.getName();
        initial.surname = existing.getSurname();
        initial.contact = existing.getContactInfo();
        initial.username = existing.getUsername();
        initial.password = existing.getPassword();
        initial.position = existing.getPosition();

        StaffFormData edited = showStaffForm(initial);
        if (edited == null) {
            return; // Cancel
        }

        try {
            // username & id değiştirmiyoruz, diğer alanları güncelliyoruz
            existing.setName(edited.name);
            existing.setSurname(edited.surname);
            existing.setContactInfo(edited.contact);
            existing.setPassword(edited.password);
            existing.setPosition(edited.position);

            staffRepo.updateStaff(existing);
            staffRepo.saveAll();
            loadStaffToTable();
            statusLabel.setText("Staff updated: " + existing.getUsername());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating staff: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException ioEx) {
            JOptionPane.showMessageDialog(this,
                    "Error saving staff file: " + ioEx.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----------------------------------------------------
    // DELETE STAFF
    // ----------------------------------------------------

    private void doDeleteStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a staff to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) staffTableModel.getValueAt(row, 4);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete staff: " + username + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deleted = staffRepo.deleteStaff(username);
            if (!deleted) {
                JOptionPane.showMessageDialog(this,
                        "Staff not found: " + username,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            staffRepo.saveAll();
            loadStaffToTable();
            statusLabel.setText("Staff deleted: " + username);
        } catch (IOException ioEx) {
            JOptionPane.showMessageDialog(this,
                    "Error saving staff file: " + ioEx.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----------------------------------------------------
    // FORM DİYALOĞU
    // ----------------------------------------------------

    private static class StaffFormData {
        String staffID;
        String name;
        String surname;
        String contact;
        String username;
        String password;
        String position;
    }

    /**
     * initialData null ise "Add", doluysa "Edit" formu gibi davranır.
     * Cancel'e basılırsa null döner.
     */
    private StaffFormData showStaffForm(StaffFormData initialData) {
        boolean isEdit = (initialData != null);

        JTextField idField       = new JTextField(10);
        JTextField nameField     = new JTextField(12);
        JTextField surnameField  = new JTextField(12);
        JTextField contactField  = new JTextField(15);
        JTextField usernameField = new JTextField(12);
        JPasswordField passwordField = new JPasswordField(12);

        String[] positions = {"EMPLOYEE", "ADMIN"};
        JComboBox<String> positionCombo = new JComboBox<>(positions);

        if (isEdit) {
            // EDIT modunda: mevcut değerleri doldur, id & username kilitli
            idField.setText(initialData.staffID);
            nameField.setText(initialData.name);
            surnameField.setText(initialData.surname);
            contactField.setText(initialData.contact);
            usernameField.setText(initialData.username);
            passwordField.setText(initialData.password != null ? initialData.password : "");
            positionCombo.setSelectedItem(initialData.position);

            idField.setEditable(false);
            usernameField.setEditable(false);
        } else {
            // ADD modunda: ID'yi otomatik üret, sadece göster (readonly)
            String previewId = staffRepo.generateNewStaffId();
            idField.setText(previewId);
            idField.setEditable(false);
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        int row = 0;

        c.gridx = 0; c.gridy = row;
        panel.add(new JLabel("Staff ID:"), c);
        c.gridx = 1;
        panel.add(idField, c);
        row++;

        c.gridx = 0; c.gridy = row;
        panel.add(new JLabel("Name:"), c);
        c.gridx = 1;
        panel.add(nameField, c);
        row++;

        c.gridx = 0; c.gridy = row;
        panel.add(new JLabel("Surname:"), c);
        c.gridx = 1;
        panel.add(surnameField, c);
        row++;

        c.gridx = 0; c.gridy = row;
        panel.add(new JLabel("Contact:"), c);
        c.gridx = 1;
        panel.add(contactField, c);
        row++;

        c.gridx = 0; c.gridy = row;
        panel.add(new JLabel("Username:"), c);
        c.gridx = 1;
        panel.add(usernameField, c);
        row++;

        c.gridx = 0; c.gridy = row;
        panel.add(new JLabel("Password:"), c);
        c.gridx = 1;
        panel.add(passwordField, c);
        row++;

        c.gridx = 0; c.gridy = row;
        panel.add(new JLabel("Position:"), c);
        c.gridx = 1;
        panel.add(positionCombo, c);
        row++;

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                isEdit ? "Edit Staff" : "Add Staff",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        StaffFormData data = new StaffFormData();
        data.staffID  = idField.getText().trim();
        data.name     = nameField.getText().trim();
        data.surname  = surnameField.getText().trim();
        data.contact  = contactField.getText().trim();
        data.username = usernameField.getText().trim();
        data.password = new String(passwordField.getPassword());
        data.position = (String) positionCombo.getSelectedItem();

        if (data.name.isEmpty() ||
            data.surname.isEmpty() ||
            data.username.isEmpty() ||
            data.password.isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Name, Surname, Username and Password cannot be empty.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return data;
    }
}
