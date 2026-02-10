package airlinesystem.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Flight {
    private final String flightNum;
    private final Route flightRoute;
    private final Plane flightPlane;

    private LocalDate date;
    private LocalTime time;
    private int flightDuration;      // minutes
    private double economyBasePrice;

    public Flight(String flightNum,
                  Route flightRoute,
                  Plane flightPlane,
                  LocalDate date,
                  LocalTime time,
                  int flightDuration,
                  double economyBasePrice) {

        if (flightNum == null || flightNum.isBlank()) {
            throw new IllegalArgumentException("Flight number cannot be null or blank");
        }
        if (flightRoute == null) {
            throw new IllegalArgumentException("Route cannot be null");
        }
        if (flightPlane == null) {
            throw new IllegalArgumentException("Plane cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (time == null) {
            throw new IllegalArgumentException("Time cannot be null");
        }
        if (flightDuration <= 0) {
            throw new IllegalArgumentException("Flight duration must be positive");
        }
        if (economyBasePrice < 0) {
            throw new IllegalArgumentException("Base price cannot be negative");
        }

        this.flightNum = flightNum;
        this.flightRoute = flightRoute;
        this.flightPlane = flightPlane;
        this.date = date;
        this.time = time;
        this.flightDuration = flightDuration;
        this.economyBasePrice = economyBasePrice;
    }

    public String getFlightNum() {
        return flightNum;
    }

    public Route getFlightRoute() {
        return flightRoute;
    }

    public Plane getFlightPlane() {
        return flightPlane;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        this.date = date;
    }

    public void setTime(LocalTime time) {
        if (time == null) {
            throw new IllegalArgumentException("Time cannot be null");
        }
        this.time = time;
    }

    public int getFlightDuration() {
        return flightDuration;
    }

    public void setFlightDuration(int flightDuration) {
        if (flightDuration <= 0) {
            throw new IllegalArgumentException("Flight duration must be positive");
        }
        this.flightDuration = flightDuration;
    }

    public double getEconomyBasePrice() {
        return economyBasePrice;
    }

    public void setEconomyBasePrice(double economyBasePrice) {
        if (economyBasePrice < 0) {
            throw new IllegalArgumentException("Base price cannot be negative");
        }
        this.economyBasePrice = economyBasePrice;
    }

    // PDF uyumu için:
    public String getDeparturePlace() {
        return flightRoute.getDeparturePlace();
    }

    public String getArrivalPlace() {
        return flightRoute.getArrivalPlace();
    }

    public LocalDateTime getDepartureDateTime() {
        return LocalDateTime.of(date, time);
    }

    public boolean isPast() {
        return getDepartureDateTime().isBefore(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Flight{" +
                "flightNum='" + flightNum + '\'' +
                ", route=" + flightRoute +
                ", date=" + date +
                ", time=" + time +
                ", duration=" + flightDuration +
                ", basePrice=" + economyBasePrice +
                '}';
    }
}
