package airlinesystem.service;

import airlinesystem.model.Plane;
import airlinesystem.model.Seat;
import airlinesystem.model.SeatClass;

public class SeatManager {

    public void createSeats(Plane plane, int businessRows, double basePrice, double multiplier) {
        if (plane == null) {
            throw new IllegalArgumentException("Plane cannot be null");
        }
        if (basePrice < 0) {
            throw new IllegalArgumentException("Base price cannot be negative");
        }
        if (multiplier <= 0) {
            throw new IllegalArgumentException("Multiplier must be positive");
        }

        int rows = plane.getRows();
        int columns = plane.getColumns();

        if (businessRows < 0) {
            businessRows = 0;
        } else if (businessRows > rows) {
            businessRows = rows;
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                char columnLetter = (char) ('A' + j);
                String seatNumber = (i + 1) + String.valueOf(columnLetter);

                SeatClass seatClass = (i < businessRows)
                        ? SeatClass.BUSINESS
                        : SeatClass.ECONOMY;

                double price = basePrice;
                if (seatClass == SeatClass.BUSINESS) {
                    price = basePrice * multiplier;
                }

                Seat seat = new Seat(seatNumber, seatClass, price);
                plane.setSeat(i, j, seat);
            }
        }
    }

    public int getAvailableSeatCount(Plane plane) {
        if (plane == null) {
            throw new IllegalArgumentException("Plane cannot be null");
        }
        int count = 0;
        for (int i = 0; i < plane.getRows(); i++) {
            for (int j = 0; j < plane.getColumns(); j++) {
                Seat seat = plane.getSeat(i, j);
                if (seat != null && !seat.isReserved()) {
                    count++;
                }
            }
        }
        return count;
    }

    public int emptySeatsCount(Plane plane) {
        return getAvailableSeatCount(plane);
    }

    public Seat resolveSeatNumber(Plane plane, String seatNumber) {
        if (plane == null) {
            throw new IllegalArgumentException("Plane cannot be null");
        }
        if (seatNumber == null || seatNumber.length() < 2) {
            return null;
        }

        seatNumber = seatNumber.trim().toUpperCase();

        char columnLetter = seatNumber.charAt(seatNumber.length() - 1);
        String rowNum = seatNumber.substring(0, seatNumber.length() - 1);

        int rowIndex;
        try {
            rowIndex = Integer.parseInt(rowNum) - 1;
        } catch (NumberFormatException e) {
            return null;
        }

        int colIndex = columnLetter - 'A';

        try {
            return plane.getSeat(rowIndex, colIndex);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void reserveSeat(Plane plane, String seatNum) {
        Seat seat = resolveSeatNumber(plane, seatNum);
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found: " + seatNum);
        }
        seat.makeReservation();
    }
}
