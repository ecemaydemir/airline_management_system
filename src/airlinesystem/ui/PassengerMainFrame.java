package airlinesystem.ui;

import airlinesystem.model.Baggage;
import airlinesystem.model.Flight;
import airlinesystem.model.Passenger;
import airlinesystem.model.Plane;
import airlinesystem.model.Seat;
import airlinesystem.model.SeatClass;
import airlinesystem.model.Reservation;
import airlinesystem.model.Ticket;
import airlinesystem.service.FlightManager;
import airlinesystem.service.ReservationManager;
import airlinesystem.service.SeatManager;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PassengerMainFrame extends JFrame {

    private final AppContext context;
    private final Passenger passenger;

    private JTextField departureField;
    private JTextField arrivalField;

    // GÜN/AY/YIL combobox'ları (String, başta boş seçenek var)
    private JComboBox<String> dayCombo;
    private JComboBox<String> monthCombo;
    private JComboBox<String> yearCombo;

    private JTable flightsTable;
    private DefaultTableModel flightsTableModel;

    // Seat seçimi için ComboBox
    private JComboBox<String> seatComboBox;
    private JTextField baggageField;
    private JTextField reservationCodeField;
    private JLabel statusLabel;

    public PassengerMainFrame(AppContext context, Passenger passenger) {
        super("Passenger Panel - " + passenger.getFullName());
        this.context = context;
        this.passenger = passenger;
        initUI();
        loadAllFlights();   // açılışta tüm uygun uçuşları göster
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 650);
        setMinimumSize(new Dimension(1150, 600));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(createSearchPanel(), BorderLayout.NORTH);
        add(createFlightsTablePanel(), BorderLayout.CENTER);
        add(createReservationPanel(), BorderLayout.SOUTH);
    }

    // -------------------- SEARCH PANEL --------------------

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel("From:"));
        departureField = new JTextField(8);
        panel.add(departureField);

        panel.add(new JLabel("To:"));
        arrivalField = new JTextField(8);
        panel.add(arrivalField);

        // === Tarih filtresi: Day / Month / Year ===

        // Day
        panel.add(new JLabel("Day:"));
        dayCombo = new JComboBox<>();
        dayCombo.addItem(""); // boş = gün filtresi yok
        for (int d = 1; d <= 31; d++) {
            dayCombo.addItem(String.valueOf(d));
        }
        dayCombo.setSelectedIndex(0);
        panel.add(dayCombo);

        // Month
        panel.add(new JLabel("Month:"));
        monthCombo = new JComboBox<>();
        monthCombo.addItem(""); // boş = ay filtresi yok
        for (int m = 1; m <= 12; m++) {
            monthCombo.addItem(String.valueOf(m));
        }
        monthCombo.setSelectedIndex(0);
        panel.add(monthCombo);

        // Year (2024 - 2030)
        panel.add(new JLabel("Year:"));
        yearCombo = new JComboBox<>();
        yearCombo.addItem(""); // boş = yıl filtresi yok
        for (int y = 2024; y <= 2030; y++) {
            yearCombo.addItem(String.valueOf(y));
        }
        yearCombo.setSelectedIndex(0);
        panel.add(yearCombo);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> doSearchFlights());
        panel.add(searchButton);

        JButton showAllButton = new JButton("Show All");
        showAllButton.addActionListener(e -> loadAllFlights());
        panel.add(showAllButton);

        return panel;
    }

    // -------------------- FLIGHTS TABLE --------------------

    private JScrollPane createFlightsTablePanel() {
        String[] columns = {
                "Flight No",
                "From",
                "To",
                "Date",
                "Time",
                "Duration (min)",
                "Plane",
                "Base Price"
        };

        flightsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tabloyu read-only yap
            }
        };
        flightsTable = new JTable(flightsTableModel);
        flightsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Satır seçildikçe koltukları yükle
        flightsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    loadAvailableSeatsForSelectedFlight();
                }
            }
        });

        return new JScrollPane(flightsTable);
    }

    // -------------------- RESERVATION PANEL --------------------

    private JPanel createReservationPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Seat combobox
        form.add(new JLabel("Seat:"));
        seatComboBox = new JComboBox<>();
        seatComboBox.setPreferredSize(new Dimension(100, 25));
        form.add(seatComboBox);

        form.add(new JLabel("Baggage (kg):"));
        baggageField = new JTextField(5);
        form.add(baggageField);

        JButton reserveButton = new JButton("Make Reservation");
        reserveButton.addActionListener(e -> doMakeReservation());
        form.add(reserveButton);

        form.add(new JLabel("Reservation Code:"));
        reservationCodeField = new JTextField(12);
        form.add(reservationCodeField);

        JButton cancelButton = new JButton("Cancel (by Code)");
        cancelButton.addActionListener(e -> doCancelReservation());
        form.add(cancelButton);

        // My Reservations (seçerek iptal + ticket gösterme)
        JButton myResButton = new JButton("My Reservations");
        myResButton.addActionListener(e -> showMyReservations());
        form.add(myResButton);

        // PROFILE BUTONU
        JButton profileButton = new JButton("Profile");
        profileButton.addActionListener(e -> showProfileDialog());
        form.add(profileButton);

        // LOGOUT BUTONU
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> doLogout());
        form.add(logoutButton);

        panel.add(form, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    // -------------------- LOAD / SEARCH FLIGHTS --------------------

    private void loadAllFlights() {
        FlightManager fm = context.getFlightManager();
        List<Flight> flights = fm.getFlights();

        flightsTableModel.setRowCount(0);
        LocalDateTime now = LocalDateTime.now();

        for (Flight f : flights) {
            // kalkış zamanı geçmişse gösterme
            LocalDateTime departureDateTime = f.getDate().atTime(f.getTime());
            if (departureDateTime.isBefore(now)) {
                continue;
            }
            flightsTableModel.addRow(new Object[]{
                    f.getFlightNum(),
                    f.getFlightRoute().getDeparturePlace(),
                    f.getFlightRoute().getArrivalPlace(),
                    f.getDate(),
                    f.getTime(),
                    f.getFlightDuration(),
                    f.getFlightPlane().getPlaneModel(),
                    f.getEconomyBasePrice()
            });
        }

        statusLabel.setText("Loaded " + flightsTableModel.getRowCount() + " upcoming flights.");
        seatComboBox.removeAllItems();
    }

    private void doSearchFlights() {
        String fromText = departureField.getText().trim();
        String toText   = arrivalField.getText().trim();

        String from = fromText.isEmpty() ? null : fromText;
        String to   = toText.isEmpty()   ? null : toText;

        // Tarih seçimi
        String dayStr   = (String) dayCombo.getSelectedItem();
        String monthStr = (String) monthCombo.getSelectedItem();
        String yearStr  = (String) yearCombo.getSelectedItem();

        LocalDate filterDate = null;
        boolean dayEmpty   = (dayStr == null || dayStr.isEmpty());
        boolean monthEmpty = (monthStr == null || monthStr.isEmpty());
        boolean yearEmpty  = (yearStr == null || yearStr.isEmpty());

        if (!dayEmpty || !monthEmpty || !yearEmpty) {
            if (dayEmpty || monthEmpty || yearEmpty) {
                JOptionPane.showMessageDialog(this,
                        "If you want to filter by date, please select Day, Month and Year.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                int d = Integer.parseInt(dayStr);
                int m = Integer.parseInt(monthStr);
                int y = Integer.parseInt(yearStr);
                filterDate = LocalDate.of(y, m, d);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid date selection (day/month/year).",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        FlightManager fm = context.getFlightManager();
        List<Flight> allFlights = fm.getFlights();

        flightsTableModel.setRowCount(0);
        LocalDateTime now = LocalDateTime.now();

        for (Flight f : allFlights) {
            LocalDateTime departureDateTime = f.getDate().atTime(f.getTime());

            // sadece gelecekteki uçuşlar
            if (departureDateTime.isBefore(now)) {
                continue;
            }

            if (from != null &&
                    !f.getFlightRoute().getDeparturePlace().equalsIgnoreCase(from)) {
                continue;
            }

            if (to != null &&
                    !f.getFlightRoute().getArrivalPlace().equalsIgnoreCase(to)) {
                continue;
            }

            // tarih filtresi varsa uygula
            if (filterDate != null && !f.getDate().equals(filterDate)) {
                continue;
            }

            flightsTableModel.addRow(new Object[]{
                    f.getFlightNum(),
                    f.getFlightRoute().getDeparturePlace(),
                    f.getFlightRoute().getArrivalPlace(),
                    f.getDate(),
                    f.getTime(),
                    f.getFlightDuration(),
                    f.getFlightPlane().getPlaneModel(),
                    f.getEconomyBasePrice()
            });
        }

        statusLabel.setText(flightsTableModel.getRowCount() == 0
                ? "No upcoming flights found for given filters."
                : flightsTableModel.getRowCount() + " upcoming flights found.");

        seatComboBox.removeAllItems();
    }

    // -------------------- AVAILABLE SEATS LOADING --------------------

    private void loadAvailableSeatsForSelectedFlight() {
        int row = flightsTable.getSelectedRow();
        if (row < 0) {
            seatComboBox.removeAllItems();
            return;
        }

        String flightNum = (String) flightsTableModel.getValueAt(row, 0);
        FlightManager fm = context.getFlightManager();
        Flight flight = fm.findByFlightNum(flightNum);
        if (flight == null) {
            seatComboBox.removeAllItems();
            return;
        }

        // Gerekirse koltukları LAZY initialize et
        ensureSeatsInitialized(flight);

        Plane plane = flight.getFlightPlane();
        seatComboBox.removeAllItems();

        for (int r = 0; r < plane.getRows(); r++) {
            for (int c = 0; c < plane.getColumns(); c++) {
                Seat seat = plane.getSeat(r, c);
                if (seat != null && !seat.isReserved()) {
                    String label = seat.getSeatNum();

                    // BUSINESS / ECONOMY etiketlemesi
                    if (seat.getSeatClass() == SeatClass.BUSINESS) {
                        label += " (B)";
                    } else {
                        label += " (E)";
                    }

                    seatComboBox.addItem(label);
                }
            }
        }
    }

    /**
     * Bu flight için seatMatrix daha önce oluşturulmamışsa burada oluşturuyoruz.
     * Admin flight eklerken zaten createSeats çağırıyorsa, bu fonksiyon ikinci kez dokunmaz.
     */
    private void ensureSeatsInitialized(Flight flight) {
        Plane plane = flight.getFlightPlane();
        try {
            // Eğer ilk koltuk null ise seatMatrix hiç oluşturulmamış kabul ediyoruz
            if (plane.getSeat(0, 0) == null) {
                SeatManager sm = context.getSeatManager();
                sm.createSeats(
                        plane,
                        2,                            // businessRows (varsayılan)
                        flight.getEconomyBasePrice(), // base price
                        1.5                           // business multiplier
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error while initializing seats: " + e.getMessage(),
                    "Seat Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------- MAKE RESERVATION --------------------

    private void doMakeReservation() {
        int row = flightsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a flight from the table.",
                    "No Flight Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selected = (String) seatComboBox.getSelectedItem();
        if (selected == null || selected.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a seat.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // "3C (B)" → "3C"
        String seatNum = selected.split(" ")[0];

        double baggageWeight = 0.0;
        String baggageText = baggageField.getText().trim();
        if (!baggageText.isEmpty()) {
            try {
                baggageWeight = Double.parseDouble(baggageText);
                if (baggageWeight < 0) {
                    throw new IllegalArgumentException("Baggage weight cannot be negative.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid baggage weight.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String flightNum = (String) flightsTableModel.getValueAt(row, 0);
        FlightManager fm = context.getFlightManager();
        Flight flight = fm.findByFlightNum(flightNum);
        if (flight == null) {
            JOptionPane.showMessageDialog(this,
                    "Selected flight not found in system.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Seatlerin initialized olduğundan emin ol
        ensureSeatsInitialized(flight);

        ReservationManager rm = context.getReservationManager();

        try {
            Baggage baggage = baggageWeight > 0 ? new Baggage(baggageWeight) : null;

            Ticket ticket = rm.makeReservation(
                    flight,
                    passenger,
                    seatNum,
                    baggage
            );

            String resCode = ticket.getReservation().getReservationCode();
            statusLabel.setText("Reservation successful. Code: " + resCode);

            JOptionPane.showMessageDialog(this,
                    "Reservation successful.\nReservation Code: " + resCode +
                            "\nTicket Price: " + ticket.getPrice(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Rezervasyon sonrası koltuk listesi güncellensin
            loadAvailableSeatsForSelectedFlight();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error during reservation: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------- CANCEL RESERVATION (KODLA) --------------------

    private void doCancelReservation() {
        String resCode = reservationCodeField.getText().trim();
        if (resCode.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a reservation code.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ReservationManager rm = context.getReservationManager();
        try {
            rm.cancelReservation(resCode);
            statusLabel.setText("Reservation cancelled: " + resCode);

            JOptionPane.showMessageDialog(this,
                    "Reservation cancelled: " + resCode,
                    "Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);

            // Aynı flight üzerindeysek boşa çıkan koltukları da güncelle
            loadAvailableSeatsForSelectedFlight();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error during cancellation: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------- MY RESERVATIONS (FİLTRELİ TABLO + TICKET GÖSTERME) --------------------

    private void showMyReservations() {
        ReservationManager rm = context.getReservationManager();
        List<Reservation> all = rm.getReservations();

        List<Reservation> mine = new ArrayList<>();
        String myId = passenger.getPassengerID();

        for (Reservation r : all) {
            if (r.getPassenger() != null &&
                    myId != null &&
                    myId.equals(r.getPassenger().getPassengerID()) &&
                    r.isActive()) { // sadece aktif rezervasyonlar
                mine.add(r);
            }
        }

        if (mine.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "You have no active reservations.",
                    "My Reservations",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] cols = {
                "Res Code",
                "Flight No",
                "From",
                "To",
                "Date",
                "Time",
                "Seat"
        };

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JDialog dialog = new JDialog(this, "My Reservations", true);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // ---------- FİLTRE PANELİ (FROM / TO / DAY / MONTH / YEAR) ----------
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JTextField fromField = new JTextField(8);
        JTextField toField   = new JTextField(8);

        filterPanel.add(new JLabel("From:"));
        filterPanel.add(fromField);

        filterPanel.add(new JLabel("To:"));
        filterPanel.add(toField);

        filterPanel.add(new JLabel("Day:"));
        JComboBox<String> dayCombo = new JComboBox<>();
        dayCombo.addItem("");
        for (int d = 1; d <= 31; d++) {
            dayCombo.addItem(String.valueOf(d));
        }
        filterPanel.add(dayCombo);

        filterPanel.add(new JLabel("Month:"));
        JComboBox<String> monthCombo = new JComboBox<>();
        monthCombo.addItem("");
        for (int m = 1; m <= 12; m++) {
            monthCombo.addItem(String.valueOf(m));
        }
        filterPanel.add(monthCombo);

        filterPanel.add(new JLabel("Year:"));
        JComboBox<String> yearCombo = new JComboBox<>();
        yearCombo.addItem("");
        for (int y = 2024; y <= 2030; y++) {
            yearCombo.addItem(String.valueOf(y));
        }
        filterPanel.add(yearCombo);

        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");

        filterPanel.add(searchBtn);
        filterPanel.add(showAllBtn);

        dialog.add(filterPanel, BorderLayout.NORTH);

        // ---------- TABLO ----------
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        // ---------- BUTON PANELİ (Cancel Selected + Show Ticket + Close) ----------
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelSelectedBtn = new JButton("Cancel Selected");
        JButton showTicketBtn = new JButton("Show Ticket");
        JButton closeBtn = new JButton("Close");
        bottomPanel.add(cancelSelectedBtn);
        bottomPanel.add(showTicketBtn);
        bottomPanel.add(closeBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        // --- Yardımcı fonksiyon: tüm "mine" listesini tabloya bas ---
        Runnable fillAll = () -> {
            model.setRowCount(0);
            for (Reservation r : mine) {
                if (!r.isActive()) {
                    continue;
                }
                Flight f = r.getFlight();
                model.addRow(new Object[]{
                        r.getReservationCode(),
                        (f != null ? f.getFlightNum() : ""),
                        (f != null ? f.getFlightRoute().getDeparturePlace() : ""),
                        (f != null ? f.getFlightRoute().getArrivalPlace() : ""),
                        (f != null ? f.getDate() : null),
                        (f != null ? f.getTime() : null),
                        (r.getSeat() != null ? r.getSeat().getSeatNum() : "")
                });
            }
        };

        // Başlangıçta hepsini göster
        fillAll.run();

        // --- Search: From / To / Date filtresi ---
        searchBtn.addActionListener(e -> {
            String fromText = fromField.getText().trim();
            String toText   = toField.getText().trim();

            String from = fromText.isEmpty() ? null : fromText;
            String to   = toText.isEmpty()   ? null : toText;

            String dayStr   = (String) dayCombo.getSelectedItem();
            String monthStr = (String) monthCombo.getSelectedItem();
            String yearStr  = (String) yearCombo.getSelectedItem();

            LocalDate filterDate = null;
            boolean dayEmpty   = (dayStr == null || dayStr.isEmpty());
            boolean monthEmpty = (monthStr == null || monthStr.isEmpty());
            boolean yearEmpty  = (yearStr == null || yearStr.isEmpty());

            if (!dayEmpty || !monthEmpty || !yearEmpty) {
                if (dayEmpty || monthEmpty || yearEmpty) {
                    JOptionPane.showMessageDialog(dialog,
                            "If you want to filter by date, please select Day, Month and Year.",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    int d = Integer.parseInt(dayStr);
                    int m = Integer.parseInt(monthStr);
                    int y = Integer.parseInt(yearStr);
                    filterDate = LocalDate.of(y, m, d);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Invalid date selection (day/month/year).",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            model.setRowCount(0);

            for (Reservation r : mine) {
                if (!r.isActive()) {
                    continue;
                }
                Flight f = r.getFlight();
                if (f == null) continue;

                if (from != null &&
                        !f.getFlightRoute().getDeparturePlace().equalsIgnoreCase(from)) {
                    continue;
                }

                if (to != null &&
                        !f.getFlightRoute().getArrivalPlace().equalsIgnoreCase(to)) {
                    continue;
                }

                if (filterDate != null && !f.getDate().equals(filterDate)) {
                    continue;
                }

                model.addRow(new Object[]{
                        r.getReservationCode(),
                        f.getFlightNum(),
                        f.getFlightRoute().getDeparturePlace(),
                        f.getFlightRoute().getArrivalPlace(),
                        f.getDate(),
                        f.getTime(),
                        (r.getSeat() != null ? r.getSeat().getSeatNum() : "")
                });
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(dialog,
                        "No reservations found for given filters.",
                        "Filter Result",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // --- Show All: filtre sıfırla ---
        showAllBtn.addActionListener(e -> {
            fromField.setText("");
            toField.setText("");
            dayCombo.setSelectedIndex(0);
            monthCombo.setSelectedIndex(0);
            yearCombo.setSelectedIndex(0);
            fillAll.run();
        });

        // Close
        closeBtn.addActionListener(e -> dialog.dispose());

        // Cancel Selected
        cancelSelectedBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Please select a reservation.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String resCode = (String) model.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(
                    dialog,
                    "Cancel reservation " + resCode + "?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                // İlgili Reservation’ı mine listesinden bul
                Reservation cancelledRes = null;
                for (Reservation r : mine) {
                    if (r.getReservationCode().equals(resCode)) {
                        cancelledRes = r;
                        break;
                    }
                }

                rm.cancelReservation(resCode);

                // Tabloyu güncelle: satırı kaldır
                model.removeRow(selectedRow);

                statusLabel.setText("Reservation cancelled: " + resCode);

                // Ana tabloda iptal edilen rezervasyonun flight'ı seçiliyse koltukları güncelle
                if (cancelledRes != null && cancelledRes.getFlight() != null) {
                    int mainRow = flightsTable.getSelectedRow();
                    if (mainRow >= 0) {
                        String mainFlightNum = (String) flightsTableModel.getValueAt(mainRow, 0);
                        if (mainFlightNum.equals(cancelledRes.getFlight().getFlightNum())) {
                            loadAvailableSeatsForSelectedFlight();
                        }
                    }
                }

                JOptionPane.showMessageDialog(dialog,
                        "Reservation cancelled: " + resCode,
                        "Cancelled",
                        JOptionPane.INFORMATION_MESSAGE);

                if (model.getRowCount() == 0) {
                    dialog.dispose();
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error during cancellation: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Show Ticket
        showTicketBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Please select a reservation.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String resCode = (String) model.getValueAt(selectedRow, 0);

            // Listeden Reservation nesnesini bul
            Reservation selectedRes = null;
            for (Reservation r : mine) {
                if (r.getReservationCode().equals(resCode)) {
                    selectedRes = r;
                    break;
                }
            }

            if (selectedRes == null) {
                JOptionPane.showMessageDialog(dialog,
                        "Reservation object not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Ticket ticket = rm.findTicketByReservationCode(resCode);
                if (ticket == null) {
                    JOptionPane.showMessageDialog(dialog,
                            "No ticket found for this reservation.",
                            "Ticket Not Found",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                showTicketDialog(selectedRes, ticket);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error while loading ticket: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    // -------------------- TICKET DIALOG --------------------

    private void showTicketDialog(Reservation reservation, Ticket ticket) {
        JDialog dialog = new JDialog(this, "Ticket Details", true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Flight Ticket", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        dialog.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 16, 6, 16);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;

        Font labelFont = new Font("SansSerif", Font.BOLD, 14);
        Font valueFont = new Font("SansSerif", Font.PLAIN, 14);

        Passenger p = reservation.getPassenger();
        Flight f = reservation.getFlight();
        Seat seat = reservation.getSeat();

        // Passport number
        String passportNo = "";
        if (p != null) {
            try {
                passportNo = p.getPassportNum();
            } catch (Exception e) {
                // boş bırak
            }
        }

        String fullName = (p != null && p.getFullName() != null) ? p.getFullName() : "";
        String resCode = reservation.getReservationCode();

        String ticketId = "";
        try {
            ticketId = ticket.getTicketID();
        } catch (Exception e) {
            // boş bırak
        }

        String seatCode = "";
        String seatClassStr = "";
        if (seat != null) {
            if (seat.getSeatNum() != null) {
                seatCode = seat.getSeatNum();
            }
            if (seat.getSeatClass() != null) {
                if (seat.getSeatClass() == SeatClass.BUSINESS) {
                    seatClassStr = "BUSINESS";
                } else {
                    seatClassStr = "ECONOMY";
                }
            }
        }

        // Reservation state
        String resState = reservation.isActive() ? "ACTIVE" : "PASSIVE";

        String departure = "";
        String arrival = "";
        String dateStr = "";
        String timeStr = "";
        if (f != null) {
            if (f.getFlightRoute() != null) {
                departure = f.getFlightRoute().getDeparturePlace();
                arrival = f.getFlightRoute().getArrivalPlace();
            }
            if (f.getDate() != null) {
                dateStr = f.getDate().toString();
            }
            if (f.getTime() != null) {
                timeStr = f.getTime().toString();
            }
        }

        String priceStr = "";
        try {
            priceStr = String.valueOf(ticket.getPrice());
        } catch (Exception e) {
            // boş bırak
        }

        java.util.function.BiConsumer<String, String> addRow = (label, value) -> {
            JLabel l = new JLabel(label);
            l.setFont(labelFont);
            c.gridx = 0;
            center.add(l, c);

            JLabel v = new JLabel(value == null ? "" : value);
            v.setFont(valueFont);
            c.gridx = 1;
            center.add(v, c);

            c.gridy++;
        };

        addRow.accept("Passport Number:", passportNo);
        addRow.accept("Name Surname:", fullName);
        addRow.accept("Reservation Code:", resCode);
        addRow.accept("Ticket ID:", ticketId);
        addRow.accept("Seat:", seatCode);
        addRow.accept("Class:", seatClassStr);
        addRow.accept("Reservation State:", resState);
        addRow.accept("Departure:", departure);
        addRow.accept("Arrival:", arrival);
        addRow.accept("Date:", dateStr);
        addRow.accept("Time:", timeStr);
        addRow.accept("Price:", priceStr);

        dialog.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        closeBtn.addActionListener(e -> dialog.dispose());
        bottom.add(closeBtn);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // -------------------- PROFILE DIALOG --------------------

    private void showProfileDialog() {
        JDialog dialog = new JDialog(this, "My Profile", true);
        dialog.setSize(450, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Passenger Profile", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        dialog.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 16, 8, 16);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;

        Font labelFont = new Font("SansSerif", Font.BOLD, 16);
        Font valueFont = new Font("SansSerif", Font.PLAIN, 16);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        center.add(usernameLabel, c);

        c.gridx = 1;
        String username = "";
        try {
            username = passenger.getUsername();
        } catch (Exception e) {
            // boş bırak
        }
        JLabel usernameValue = new JLabel(username == null ? "" : username);
        usernameValue.setFont(valueFont);
        center.add(usernameValue, c);

        // Full Name
        c.gridx = 0;
        c.gridy++;
        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setFont(labelFont);
        center.add(fullNameLabel, c);

        c.gridx = 1;
        String fullName = passenger.getFullName();
        JLabel fullNameValue = new JLabel(fullName == null ? "" : fullName);
        fullNameValue.setFont(valueFont);
        center.add(fullNameValue, c);

        // Contact Info
        c.gridx = 0;
        c.gridy++;
        JLabel contactLabel = new JLabel("Contact Info:");
        contactLabel.setFont(labelFont);
        center.add(contactLabel, c);

        c.gridx = 1;
        String contactInfo = "";
        try {
            contactInfo = passenger.getContactInfo();
        } catch (Exception e) {
            // boş bırak
        }
        JLabel contactValue = new JLabel(contactInfo == null ? "" : contactInfo);
        contactValue.setFont(valueFont);
        center.add(contactValue, c);

        // Passport No
        c.gridx = 0;
        c.gridy++;
        JLabel passportLabel = new JLabel("Passport No:");
        passportLabel.setFont(labelFont);
        center.add(passportLabel, c);

        c.gridx = 1;
        String passportNo = "";
        try {
            passportNo = passenger.getPassportNum();
        } catch (Exception e) {
            // boş bırak
        }
        JLabel passportValue = new JLabel(passportNo == null ? "" : passportNo);
        passportValue.setFont(valueFont);
        center.add(passportValue, c);

        dialog.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        closeBtn.addActionListener(e -> dialog.dispose());
        bottom.add(closeBtn);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // -------------------- LOGOUT --------------------

    private void doLogout() {
        this.dispose();
        new LoginFrame(context).setVisible(true);
    }
}
