package airlinesystem.model;

import java.time.LocalDateTime;

public class Reservation {

    private final String reservationCode;
    private final Flight flight;
    private final Passenger passenger;
    private final Seat seat;
    private final LocalDateTime reservationDate;
    private boolean active;

    public Reservation(String reservationCode,
                       Flight flight,
                       Passenger passenger,
                       Seat seat,
                       LocalDateTime reservationDate) {

        if (reservationCode == null || reservationCode.isBlank()) {
            throw new IllegalArgumentException("Reservation code cannot be null or blank");
        }
        if (flight == null || passenger == null || seat == null || reservationDate == null) {
            throw new IllegalArgumentException("Flight, passenger, seat and reservation date cannot be null");
        }

        this.reservationCode = reservationCode;
        this.flight = flight;
        this.passenger = passenger;
        this.seat = seat;
        this.reservationDate = reservationDate;
        this.active = true;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public Flight getFlight() {
        return flight;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public Seat getSeat() {
        return seat;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public boolean isActive() {
        return active;
    }
    

    // Kontrollü iptal
    public void cancel() {
        if (!active) {
            throw new IllegalStateException("Reservation is already cancelled");
        }
        this.active = false;
        if (seat.isReserved()) {
            seat.cancelReservation();
        }
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "code='" + reservationCode + '\'' +
                ", flight=" + flight.getFlightNum() +
                ", passenger=" + passenger.getFullName() +
                ", seat=" + seat.getSeatNum() +
                ", active=" + active +
                '}';
    }
}
