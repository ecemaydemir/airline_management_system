package airlinesystem.model;

public class Ticket {

    private final String ticketID;
    private final Reservation reservation;
    private double price;              // final calculated price
    private double baggageAllowance;   // free baggage allowance in kg
    private Baggage baggage;          // actual baggage (can be null)

    public Ticket(String ticketID,
                  Reservation reservation,
                  double price,
                  double baggageAllowance,
                  Baggage baggage) {

        if (ticketID == null || ticketID.isBlank()) {
            throw new IllegalArgumentException("Ticket ID cannot be null or blank");
        }
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (baggageAllowance < 0) {
            throw new IllegalArgumentException("Baggage allowance cannot be negative");
        }
        if (baggage != null && baggage.getWeight() < 0) {
            throw new IllegalArgumentException("Baggage weight cannot be negative");
        }

        this.ticketID = ticketID;
        this.reservation = reservation;
        this.price = price;
        this.baggageAllowance = baggageAllowance;
        this.baggage = baggage;
    }

    public String getTicketID() {
        return ticketID;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public double getPrice() {
        return price;
    }

    // PriceCalculator burayı kullanacak
    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    public double getBaggageAllowance() {
        return baggageAllowance;
    }

    public void setBaggageAllowance(double baggageAllowance) {
        if (baggageAllowance < 0) {
            throw new IllegalArgumentException("Baggage allowance cannot be negative");
        }
        this.baggageAllowance = baggageAllowance;
    }

    public Baggage getBaggage() {
        return baggage;
    }

    public void setBaggage(Baggage baggage) {
        if (baggage != null && baggage.getWeight() < 0) {
            throw new IllegalArgumentException("Baggage weight cannot be negative");
        }
        this.baggage = baggage;
    }

    public double getBaggageWeight() {
        return baggage != null ? baggage.getWeight() : 0.0;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketID='" + ticketID + '\'' +
                ", reservationCode=" + reservation.getReservationCode() +
                ", price=" + price +
                ", baggageAllowance=" + baggageAllowance +
                ", baggageWeight=" + getBaggageWeight() +
                '}';
    }
}
