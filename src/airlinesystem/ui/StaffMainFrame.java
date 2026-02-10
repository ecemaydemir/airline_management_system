package airlinesystem.ui;

import airlinesystem.concurrent.ReportGenerator;
import airlinesystem.concurrent.ReportListener;
import airlinesystem.concurrent.SeatReservationSimulation;
import airlinesystem.model.Flight;
import airlinesystem.model.Plane;
import airlinesystem.model.Route;
import airlinesystem.model.Staff;
import airlinesystem.repository.FlightFileRepository;
import airlinesystem.repository.PlaneFileRepository;
import airlinesystem.service.FlightManager;
import airlinesystem.service.SeatManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class StaffMainFrame extends JFrame {

    private static final int DEFAULT_BUSINESS_ROWS = 2; // <-- her uçakta 2 sıra business

    private final AppContext context;
    private final Staff staff;

    // flights.txt için repo
    private final FlightFileRepository flightRepo;

    private JTable flightsTable;
    private DefaultTableModel flightsTableModel;
    private JTextArea outputArea;
    private JLabel statusLabel;

    public StaffMainFrame(AppContext context, Staff staff) {
        super("Staff Panel - " + staff.getFullName());
        this.context = context;
        this.staff = staff;

        this.flightRepo = new FlightFileRepository("flights.txt");

        initUI();
        loadFlightsToTable();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createFlightsPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane createFlightsPanel() {
        String[] cols = {
                "Flight No",
                "From",
                "To",
                "Date",
                "Time",
                "Duration (min)",
                "Plane",
                "Base Price"
        };

        flightsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        flightsTable = new JTable(flightsTableModel);
        flightsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        flightsTable.getColumnModel().getColumn(0).setPreferredWidth(80);   // Flight No
        flightsTable.getColumnModel().getColumn(1).setPreferredWidth(60);   // From
        flightsTable.getColumnModel().getColumn(2).setPreferredWidth(60);   // To
        flightsTable.getColumnModel().getColumn(3).setPreferredWidth(110);  // Date
        flightsTable.getColumnModel().getColumn(4).setPreferredWidth(70);   // Time
        flightsTable.getColumnModel().getColumn(5).setPreferredWidth(110);  // Duration
        flightsTable.getColumnModel().getColumn(6).setPreferredWidth(150);  // Plane
        flightsTable.getColumnModel().getColumn(7).setPreferredWidth(90);   // Base Price

        return new JScrollPane(flightsTable);
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(400, 0));

        // 8 satır: Add, Edit, Remove, Manage Staff, Sync, Async, Report, Logout
        JPanel buttons = new JPanel(new GridLayout(8, 1, 5, 5));

        JButton addFlightBtn = new JButton("Add Flight");
        addFlightBtn.addActionListener(e -> doAddFlight());
        buttons.add(addFlightBtn);

        JButton editFlightBtn = new JButton("Edit Selected Flight");
        editFlightBtn.addActionListener(e -> doEditFlight());
        buttons.add(editFlightBtn);

        JButton removeFlightBtn = new JButton("Remove Selected Flight");
        removeFlightBtn.addActionListener(e -> doRemoveFlight());
        buttons.add(removeFlightBtn);

        JButton manageStaffBtn = new JButton("Manage Staff");
        manageStaffBtn.addActionListener(e -> openStaffManagement());
        buttons.add(manageStaffBtn);

        JButton syncSimBtn = new JButton("Run Simulation (Sync)");
        syncSimBtn.addActionListener(e -> runSimulation(true));
        buttons.add(syncSimBtn);

        JButton asyncSimBtn = new JButton("Run Simulation (Async)");
        asyncSimBtn.addActionListener(e -> runSimulation(false));
        buttons.add(asyncSimBtn);

        JButton reportBtn = new JButton("Generate Report");
        reportBtn.addActionListener(e -> runReport());
        buttons.add(reportBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> doLogout());
        buttons.add(logoutBtn);

        panel.add(buttons, BorderLayout.NORTH);

        // ORTA: sadece text output (seat map burada yok, ayrı dialog’da açılıyor)
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(outputArea);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, BorderLayout.CENTER);
        return panel;
    }

    private void loadFlightsToTable() {
        flightsTableModel.setRowCount(0);
        FlightManager fm = context.getFlightManager();
        List<Flight> flights = fm.getFlights();

        for (Flight f : flights) {
            flightsTableModel.addRow(new Object[]{
                    f.getFlightNum(),
                    f.getDeparturePlace(),
                    f.getArrivalPlace(),
                    f.getDate(),
                    f.getTime(),
                    f.getFlightDuration(),
                    f.getFlightPlane().getPlaneModel(),
                    f.getEconomyBasePrice()
            });
        }
    }

    // -------------------- FLIGHT NUMBER GENERATION --------------------

    /**
     * Mevcut uçuşlara bakarak F001, F002, ... şeklinde yeni bir flight number üretir.
     * Sadece "F" ile başlayan numaraları dikkate alır.
     */
    private String generateNewFlightNumber() {
        FlightManager fm = context.getFlightManager();
        int max = 0;
        for (Flight f : fm.getFlights()) {
            String fn = f.getFlightNum();
            if (fn == null) continue;
            if (fn.startsWith("F") && fn.length() > 1) {
                try {
                    int n = Integer.parseInt(fn.substring(1));
                    if (n > max) {
                        max = n;
                    }
                } catch (NumberFormatException ignore) {
                    // "FABC" gibi saçma bir şey varsa yok say
                }
            }
        }
        int next = max + 1;
        return String.format("F%03d", next);  // F001, F002, ...
    }

    // -------------------- FLIGHT ADD / EDIT / REMOVE --------------------

    private void doAddFlight() {
        if (!staff.isAdmin()) {
            JOptionPane.showMessageDialog(this,
                    "Only ADMIN can add flights.",
                    "Permission Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            PlaneFileRepository planeRepo = new PlaneFileRepository("plane.txt");
            List<Plane> templatePlanes = planeRepo.loadAll();
            if (templatePlanes.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No planes defined in plane.txt",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Artık Flight No alanı yok – sistem üretecek
            JTextField fromField      = new JTextField(6);
            JTextField toField        = new JTextField(6);
            JTextField basePriceField = new JTextField(8);
            JTextField dateField      = new JTextField(10);  // yyyy-MM-dd
            JTextField timeField      = new JTextField(5);   // HH:mm
            JTextField durationField  = new JTextField(5);   // dakika

            LocalDate defaultDate = LocalDate.now().plusDays(1);
            dateField.setText(defaultDate.toString());
            timeField.setText("12:00");
            durationField.setText("60");

            JComboBox<Plane> planeCombo =
                    new JComboBox<>(templatePlanes.toArray(new Plane[0]));

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);
            c.anchor = GridBagConstraints.WEST;
            int row = 0;


            c.gridx = 0; c.gridy = row;
            panel.add(new JLabel("From:"), c);
            c.gridx = 1;
            panel.add(fromField, c);
            row++;

            c.gridx = 0; c.gridy = row;
            panel.add(new JLabel("To:"), c);
            c.gridx = 1;
            panel.add(toField, c);
            row++;

            c.gridx = 0; c.gridy = row;
            panel.add(new JLabel("Base Price:"), c);
            c.gridx = 1;
            panel.add(basePriceField, c);
            row++;

            c.gridx = 0; c.gridy = row;
            panel.add(new JLabel("Date (yyyy-MM-dd):"), c);
            c.gridx = 1;
            panel.add(dateField, c);
            row++;

            c.gridx = 0; c.gridy = row;
            panel.add(new JLabel("Time (HH:mm):"), c);
            c.gridx = 1;
            panel.add(timeField, c);
            row++;

            c.gridx = 0; c.gridy = row;
            panel.add(new JLabel("Duration (minutes):"), c);
            c.gridx = 1;
            panel.add(durationField, c);
            row++;

            c.gridx = 0; c.gridy = row;
            panel.add(new JLabel("Plane:"), c);
            c.gridx = 1;
            panel.add(planeCombo, c);
            row++;

            int result = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "New Flight",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String from      = fromField.getText().trim();
            String to        = toField.getText().trim();
            String baseStr   = basePriceField.getText().trim();
            String dateStr   = dateField.getText().trim();
            String timeStr   = timeField.getText().trim();
            String durStr    = durationField.getText().trim();
            Plane selectedTemplate = (Plane) planeCombo.getSelectedItem();

            if (from.isEmpty() || to.isEmpty()
                    || baseStr.isEmpty() || dateStr.isEmpty() || timeStr.isEmpty()
                    || durStr.isEmpty() || selectedTemplate == null) {
                throw new IllegalArgumentException("All fields must be filled.");
            }

            double basePrice   = Double.parseDouble(baseStr);
            LocalDate date     = LocalDate.parse(dateStr);
            LocalTime time     = LocalTime.parse(timeStr);
            int duration       = Integer.parseInt(durStr);

            if (duration <= 0) {
                throw new IllegalArgumentException("Duration must be positive.");
            }

            Route route = new Route(from, to);

            String planeId = selectedTemplate.getPlaneID();

            Plane planeForFlight = new Plane(
                    planeId,
                    selectedTemplate.getPlaneModel(),
                    selectedTemplate.getRows(),
                    selectedTemplate.getColumns()
            );

            // Her uçak için sabit 2 business row
            SeatManager sm = context.getSeatManager();
            sm.createSeats(
                    planeForFlight,
                    DEFAULT_BUSINESS_ROWS,
                    basePrice,
                    1.5
            );

            // Uçuş numarasını sistem otomatik üretiyor
            String flightNum = generateNewFlightNumber();

            Flight flight = new Flight(
                    flightNum,
                    route,
                    planeForFlight,
                    date,
                    time,
                    duration,
                    basePrice
            );

            FlightManager fm = context.getFlightManager();
            fm.addFlight(staff, flight);

            try {
                flightRepo.saveAll(fm.getFlights());
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this,
                        "Error saving flights: " + ioe.getMessage(),
                        "File Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            loadFlightsToTable();
            statusLabel.setText("Flight added: " + flightNum);

            JOptionPane.showMessageDialog(this,
                    "Flight created.\nFlight No: " + flightNum,
                    "Flight Added",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid numeric value (price/duration).",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error adding flight: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doEditFlight() {
        if (!staff.isAdmin()) {
            JOptionPane.showMessageDialog(this,
                    "Only ADMIN can edit flights.",
                    "Permission Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int row = flightsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a flight to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String flightNum = (String) flightsTableModel.getValueAt(row, 0);
        FlightManager fm = context.getFlightManager();
        Flight original = fm.findByFlightNum(flightNum);

        if (original == null) {
            JOptionPane.showMessageDialog(this,
                    "Flight not found: " + flightNum,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String from = JOptionPane.showInputDialog(this,
                    "Departure:", original.getFlightRoute().getDeparturePlace());
            if (from == null || from.isBlank()) return;

            String to = JOptionPane.showInputDialog(this,
                    "Arrival:", original.getFlightRoute().getArrivalPlace());
            if (to == null || to.isBlank()) return;

            String dateStr = JOptionPane.showInputDialog(this,
                    "Date (yyyy-MM-dd):", original.getDate().toString());
            if (dateStr == null || dateStr.isBlank()) return;

            String timeStr = JOptionPane.showInputDialog(this,
                    "Time (HH:mm):", original.getTime().toString());
            if (timeStr == null || timeStr.isBlank()) return;

            String durationStr = JOptionPane.showInputDialog(this,
                    "Duration (minutes):", String.valueOf(original.getFlightDuration()));
            if (durationStr == null || durationStr.isBlank()) return;

            String basePriceStr = JOptionPane.showInputDialog(this,
                    "Economy Base Price:", String.valueOf(original.getEconomyBasePrice()));
            if (basePriceStr == null || basePriceStr.isBlank()) return;

            LocalDate date = LocalDate.parse(dateStr.trim());
            LocalTime time = LocalTime.parse(timeStr.trim());
            int duration = Integer.parseInt(durationStr.trim());
            double basePrice = Double.parseDouble(basePriceStr.trim());

            Route newRoute = new Route(from.trim(), to.trim());

            Flight updated = new Flight(
                    original.getFlightNum(),
                    newRoute,
                    original.getFlightPlane(),
                    date,
                    time,
                    duration,
                    basePrice
            );

            boolean ok = fm.updateFlight(staff, updated);
            if (ok) {
                try {
                    flightRepo.saveAll(fm.getFlights());
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(this,
                            "Error saving flights: " + ioe.getMessage(),
                            "File Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                statusLabel.setText("Flight updated: " + updated.getFlightNum());
            } else {
                statusLabel.setText("Flight not found to update: " + updated.getFlightNum());
            }
            loadFlightsToTable();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error editing flight: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRemoveFlight() {
        if (!staff.isAdmin()) {
            JOptionPane.showMessageDialog(this,
                    "Only ADMIN can remove flights.",
                    "Permission Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int row = flightsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a flight to remove.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String flightNum = (String) flightsTableModel.getValueAt(row, 0);
        FlightManager fm = context.getFlightManager();

        int confirm =
                JOptionPane.showConfirmDialog(this,
                        "Delete flight " + flightNum + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean removed;
        try {
            removed = fm.removeFlight(staff, flightNum);
        } catch (SecurityException se) {
            JOptionPane.showMessageDialog(this,
                    "Only admin staff can remove flights.",
                    "Permission Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (removed) {
            try {
                flightRepo.saveAll(fm.getFlights());
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this,
                        "Error saving flights: " + ioe.getMessage(),
                        "File Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            loadFlightsToTable();
            statusLabel.setText("Flight removed: " + flightNum);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Flight not found: " + flightNum,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------- STAFF MANAGEMENT --------------------

    private void openStaffManagement() {
        if (!staff.isAdmin()) {
            JOptionPane.showMessageDialog(this,
                    "Only ADMIN can manage staff.",
                    "Permission Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        StaffManagementDialog dialog =
                new StaffManagementDialog(this, context.getStaffRepo());
        dialog.setVisible(true);
    }

    // -------------------- SIMULATION & REPORT --------------------

    private void runSimulation(boolean synchronizedMode) {
        statusLabel.setText("Running simulation... ("
                + (synchronizedMode ? "synchronized" : "unsynchronized") + ")");

        new Thread(() -> {
            try {
                SeatReservationSimulation sim = new SeatReservationSimulation();
                SeatReservationSimulation.SimulationResult result =
                        sim.runSimulation(synchronizedMode);

                String msg = String.format(
                        "Simulation (%s) -> Reserved: %d, Empty: %d%n",
                        synchronizedMode ? "SYNC" : "ASYNC",
                        result.getReservedCount(),
                        result.getEmptyCount()
                );

                boolean[] seatStates = result.getSeatOccupied(); // seat map verisi

                SwingUtilities.invokeLater(() -> {
                    // Metin alanına yaz
                    outputArea.append(msg);
                    statusLabel.setText("Simulation finished.");

                    // Yeni pencereyi aç (6 sütun kullanıyoruz; 30x6 = 180 koltuk)
                    String title = synchronizedMode
                            ? "Seat Map - SYNCHRONIZED"
                            : "Seat Map - ASYNCHRONIZED";

                    SeatMapDialog dialog =
                            new SeatMapDialog(StaffMainFrame.this, title, seatStates, 6);
                    dialog.setVisible(true);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Error during simulation: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Simulation error.");
                });
            }
        }, "SimulationThread").start();
    }

    private void runReport() {
        statusLabel.setText("Preparing report...");
        outputArea.append("Preparing report at " + LocalDateTime.now() + "...\n");

        ReportListener listener = reportText -> SwingUtilities.invokeLater(() -> {
            outputArea.append("\n=== REPORT COMPLETED ===\n");
            outputArea.append(reportText);
            outputArea.append("\n");
            statusLabel.setText("Report completed.");
        });

        ReportGenerator generator = new ReportGenerator(
                context.getFlightManager(),
                context.getReservationManager(),
                listener,
                3000
        );

        Thread t = new Thread(generator, "ReportGeneratorThread");
        t.start();
    }

    // -------------------- LOGOUT --------------------

    private void doLogout() {
        this.dispose();
        new LoginFrame(context).setVisible(true);
    }
}
