package airlinesystem.repository;

import airlinesystem.model.Baggage;
import airlinesystem.model.Flight;
import airlinesystem.model.Passenger;
import airlinesystem.model.Reservation;
import airlinesystem.model.Seat;
import airlinesystem.model.Ticket;
import airlinesystem.service.FlightManager;
import airlinesystem.service.ReservationManager;
import airlinesystem.service.SeatManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationFileRepository {

    private final Path filePath;

    public ReservationFileRepository(String fileName) {
        this.filePath = Path.of(fileName);
    }

    /**
     * Satır formatı (13 alan):
     * 0  reservationCode
     * 1  ticketID
     * 2  flightNum
     * 3  passengerID
     * 4  passengerName
     * 5  contactInfo
     * 6  passportNum
     * 7  seatNum
     * 8  price
     * 9  baggageAllowance
     * 10 baggageWeight
     * 11 active
     * 12 createdAt (ISO-8601)
     */
    public void loadAll(ReservationManager reservationManager,
                        FlightManager flightManager,
                        PassengerFileRepository passengerRepo,
                        SeatManager seatManager) throws IOException {

        List<Reservation> loadedReservations = new ArrayList<>();
        List<Ticket> loadedTickets = new ArrayList<>();

        if (!Files.exists(filePath)) {
            reservationManager.loadData(loadedReservations, loadedTickets);
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
                if (parts.length != 13) {
                    continue; // format bozuk
                }

                String reservationCode   = parts[0];
                String ticketID          = parts[1];
                String flightNum         = parts[2];
                String passengerID       = parts[3];
                String passengerName     = parts[4];
                String contactInfo       = parts[5];
                String passportNum       = parts[6];
                String seatNum           = parts[7];
                double price             = Double.parseDouble(parts[8]);
                double baggageAllowance  = Double.parseDouble(parts[9]);
                double baggageWeight     = Double.parseDouble(parts[10]);
                boolean active           = Boolean.parseBoolean(parts[11]);
                LocalDateTime createdAt  = LocalDateTime.parse(parts[12]);

                // 1) Flight
                Flight flight = flightManager.findByFlightNum(flightNum);
                if (flight == null) {
                    continue;
                }

                // 2) Passenger (repo'dan bul, yoksa minimal yarat)
                Passenger passenger = passengerRepo.findById(passengerID);
                if (passenger == null) {
                    String name = passengerName;
                    String surname = "";
                    int idx = passengerName.lastIndexOf(' ');
                    if (idx > 0) {
                        name = passengerName.substring(0, idx);
                        surname = passengerName.substring(idx + 1);
                    }
                    passenger = new Passenger(
                            passengerID,
                            name,
                            surname,
                            contactInfo,
                            passportNum
                    );
                }

                // 3) Seat
                Seat seat = seatManager.resolveSeatNumber(flight.getFlightPlane(), seatNum);
                if (seat == null) {
                    continue;
                }

                // 4) Reservation – constructor default olarak active = true yapıyorsa:
                Reservation reservation = new Reservation(
                        reservationCode,
                        flight,
                        passenger,
                        seat,
                        createdAt
                );

                // 5) Baggage
                Baggage baggage = baggageWeight > 0 ? new Baggage(baggageWeight) : null;

                // 6) Ticket
                Ticket ticket = new Ticket(
                        ticketID,
                        reservation,
                        price,
                        baggageAllowance,
                        baggage
                );

                // 7) Aktif / pasif durumuna göre state ayarı
                if (active) {
                    // Rezervasyon aktifse koltuk da rezerve olmalı
                    if (!seat.isReserved()) {
                        seat.makeReservation();
                    }

                } else {
                    reservation.cancel();
                }

                loadedReservations.add(reservation);
                loadedTickets.add(ticket);
            }
        }

        reservationManager.loadData(loadedReservations, loadedTickets);
    }

    public void saveAll(List<Reservation> reservations,
                        List<Ticket> tickets) throws IOException {

        Map<String, Ticket> ticketByResCode = new HashMap<>();
        for (Ticket t : tickets) {
            String resCode = t.getReservation().getReservationCode();
            ticketByResCode.put(resCode, t);
        }

        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            for (Reservation r : reservations) {
                Ticket ticket = ticketByResCode.get(r.getReservationCode());
                if (ticket == null) {
                    continue;
                }

                String reservationCode   = r.getReservationCode();
                String ticketID          = ticket.getTicketID();
                String flightNum         = r.getFlight().getFlightNum();

                Passenger p              = r.getPassenger();
                String passengerID       = p.getPassengerID();
                String passengerName     = p.getFullName();
                String contactInfo       = p.getContactInfo();
                String passportNum       = p.getPassportNum();

                String seatNum           = r.getSeat().getSeatNum();
                double price             = ticket.getPrice();
                double baggageAllowance  = ticket.getBaggageAllowance();
                double baggageWeight     = (ticket.getBaggage() != null)
                        ? ticket.getBaggage().getWeight()
                        : 0.0;
                boolean active           = r.isActive();
                String createdAt         = r.getReservationDate().toString();

                String line = String.join(";",
                        reservationCode,
                        ticketID,
                        flightNum,
                        passengerID,
                        passengerName,
                        contactInfo,
                        passportNum,
                        seatNum,
                        String.valueOf(price),
                        String.valueOf(baggageAllowance),
                        String.valueOf(baggageWeight),
                        String.valueOf(active),
                        createdAt
                );

                bw.write(line);
                bw.newLine();
            }
        }
    }
}
