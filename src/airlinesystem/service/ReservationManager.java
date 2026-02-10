package airlinesystem.service;

import airlinesystem.model.*;
import airlinesystem.repository.ReservationFileRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationManager {

    private static final double DEFAULT_BAGGAGE_ALLOWANCE_KG = 15.0;

    private final SeatManager seatManager;
    private final PriceCalculator priceCalculator;
    private final ReservationFileRepository reservationRepo;

    private final List<Reservation> reservations = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();

    public ReservationManager(SeatManager seatManager,
                              PriceCalculator priceCalculator,
                              ReservationFileRepository reservationRepo) {
        if (seatManager == null) {
            throw new IllegalArgumentException("SeatManager cannot be null");
        }
        if (priceCalculator == null) {
            throw new IllegalArgumentException("PriceCalculator cannot be null");
        }
        if (reservationRepo == null) {
            throw new IllegalArgumentException("ReservationFileRepository cannot be null");
        }
        this.seatManager = seatManager;
        this.priceCalculator = priceCalculator;
        this.reservationRepo = reservationRepo;
    }

    public List<Reservation> getReservations() {
        return List.copyOf(reservations);
    }

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    public Reservation findReservationWithCode(String code) {
        if (code == null) {
            return null;
        }
        for (Reservation r : reservations) {
            if (code.equals(r.getReservationCode())) {
                return r;
            }
        }
        return null;
    }

    private String createReservationCode(Flight flight, Passenger passenger, String seatNum) {
        return flight.getFlightNum() + "-" + passenger.getPassengerID() + "-" + seatNum;
    }

    private String createTicketId(Reservation reservation) {
        return "T-" + reservation.getReservationCode();
    }

    private void saveToFile() {
        try {
            reservationRepo.saveAll(reservations, tickets);
        } catch (IOException e) {
            System.err.println("reservations.txt kaydedilemedi: " + e.getMessage());
        }
    }

    public synchronized Ticket makeReservation(Flight flight,
                                               Passenger passenger,
                                               String seatNum,
                                               Baggage baggage) {
        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null");
        }
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }
        if (seatNum == null || seatNum.isBlank()) {
            throw new IllegalArgumentException("Seat number cannot be null or blank");
        }

        // Koltuk çöz ve uygun mu kontrol et
        Seat seat = seatManager.resolveSeatNumber(flight.getFlightPlane(), seatNum);
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found: " + seatNum);
        }
        if (seat.isReserved()) {
            throw new IllegalStateException("Seat already reserved: " + seatNum);
        }

        // Koltuğu rezerve et
        seat.makeReservation();

        // Reservation oluştur
        String reservationCode = createReservationCode(flight, passenger, seatNum);
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation(reservationCode, flight, passenger, seat, now);
        reservations.add(reservation);

        // Baggage allowance (sistem genelinde sabit)
        double baggageAllowance = DEFAULT_BAGGAGE_ALLOWANCE_KG;

        // Fiyatı hesapla
        double price = priceCalculator.calculatePrice(
                flight,
                seat,
                baggageAllowance,
                baggage
        );

        // Ticket oluştur
        String ticketId = createTicketId(reservation);
        Ticket ticket = new Ticket(ticketId, reservation, price, baggageAllowance, baggage);
        tickets.add(ticket);

        // Dosyaya yaz
        saveToFile();

        return ticket;
    }

    public synchronized void cancelReservation(String reservationCode) {
        if (reservationCode == null || reservationCode.isBlank()) {
            throw new IllegalArgumentException("Reservation code cannot be null or blank");
        }

        Reservation reservation = findReservationWithCode(reservationCode);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found: " + reservationCode);
        }

        if (!reservation.isActive()) {
            return; // zaten iptal edilmiş
        }

        // Reservation kendi iptal mantığını uygulasın (seat.cancelReservation dahil)
        reservation.cancel();

        // İlgili bileti listeden çıkar
        tickets.removeIf(t -> t.getReservation() == reservation);

        // Rezervasyonu da listeden tamamen çıkar
        reservations.remove(reservation);

        // Dosyaya yaz
        saveToFile();
    }

    public void loadData(List<Reservation> loadedReservations,
                         List<Ticket> loadedTickets) {

        this.reservations.clear();
        this.tickets.clear();

        // Sadece aktif rezervasyonları al
        for (Reservation r : loadedReservations) {
            if (r.isActive()) {
                this.reservations.add(r);
            }
        }

        // Sadece rezervasyonu hâlâ aktif olan biletleri al
        for (Ticket t : loadedTickets) {
            if (t.getReservation() != null && t.getReservation().isActive()) {
                this.tickets.add(t);
            }
        }
    }
    public Ticket findTicketByReservationCode(String resCode) {
        if (resCode == null) {
            return null;
        }

        // Önce Reservation'ı senin var olan methodla bul
        Reservation res = findReservationWithCode(resCode);
        if (res == null) {
            return null;
        }

        // tickets listeni burada gez: tickets alanı zaten bu sınıfta var 
        for (Ticket t : tickets) {
            if (t == null) continue;

            Reservation tr = t.getReservation();
            if (tr == res) {
                return t;
            }
            if (tr != null && resCode.equals(tr.getReservationCode())) {
                return t;
            }
        }

        return null;
    }

}
