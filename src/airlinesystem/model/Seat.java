package airlinesystem.model;

public class Seat {

    private final String seatNum;
    private final SeatClass seatClass;
    private double basePrice;
    private boolean reserved;

    public Seat(String seatNum, SeatClass seatClass, double basePrice) {

        if (seatNum == null || seatNum.isBlank()) {
            throw new IllegalArgumentException("Seat number cannot be null or blank");
        }
        if (seatClass == null) {
            throw new IllegalArgumentException("Seat class cannot be null");
        }
        if (basePrice < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        this.seatNum = seatNum;
        this.seatClass = seatClass;
        this.basePrice = basePrice;
        this.reserved = false;
    }

    public String getSeatNum() {
        return seatNum;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.basePrice = price;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void makeReservation() {
        if (reserved) {
            throw new IllegalStateException("Seat is already reserved");
        }
        reserved = true;
    }

    public void cancelReservation() {
        if (!reserved) {
            throw new IllegalStateException("Seat is not reserved");
        }
        reserved = false;
    }

    @Override
    public String toString() {
        return seatNum + " (" + seatClass + ", reserved=" + reserved + ")";
    }
}
