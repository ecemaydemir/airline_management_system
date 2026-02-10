package airlinesystem.service;

import airlinesystem.model.Flight;
import airlinesystem.model.Staff;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlightManager {

    private final List<Flight> flights = new ArrayList<>();

    public List<Flight> getFlights() {
        return List.copyOf(flights);
    }

    private void requireAdmin(Staff staff) {
        if (staff == null || !staff.isAdmin()) {
            throw new SecurityException("Only admin staff can manage flights");
        }
    }

    public void addFlight(Staff staff, Flight flight) {
        requireAdmin(staff);
        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null");
        }

        if (findByFlightNum(flight.getFlightNum()) != null) {
            throw new IllegalArgumentException("Flight number already exists: " + flight.getFlightNum());
        }

        flights.add(flight);
    }

    public Flight findByFlightNum(String flightNum) {
        if (flightNum == null) return null;
        for (Flight f : flights) {
            if (flightNum.equals(f.getFlightNum())) {
                return f;
            }
        }
        return null;
    }

    public boolean removeFlight(Staff staff, String flightNum) {
        requireAdmin(staff);
        if (flightNum == null) return false;

        Iterator<Flight> it = flights.iterator();
        while (it.hasNext()) {
            Flight f = it.next();
            if (flightNum.equals(f.getFlightNum())) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public boolean updateFlight(Staff staff, Flight updated) {
        requireAdmin(staff);
        if (updated == null) return false;
        String flightNum = updated.getFlightNum();
        if (flightNum == null) return false;

        for (int i = 0; i < flights.size(); i++) {
            if (flightNum.equals(flights.get(i).getFlightNum())) {
                flights.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public List<Flight> searchAvailableFlights(String departureCity,
                                               String arrivalCity,
                                               LocalDateTime now) {
        if (departureCity == null || arrivalCity == null) {
            throw new IllegalArgumentException("Departure and arrival cities cannot be null");
        }
        if (now == null) {
            now = LocalDateTime.now();
        }

        List<Flight> result = new ArrayList<>();

        for (Flight f : flights) {
            boolean matchRoute =
                    f.getDeparturePlace().equalsIgnoreCase(departureCity) &&
                    f.getArrivalPlace().equalsIgnoreCase(arrivalCity);

            LocalDateTime departureDateTime = f.getDepartureDateTime();
            boolean notDepartedYet = !departureDateTime.isBefore(now);

            if (matchRoute && notDepartedYet) {
                result.add(f);
            }
        }

        return result;
    }

    public List<Flight> filterFutureFlights(LocalDateTime now) {
        if (now == null) {
            now = LocalDateTime.now();
        }
        List<Flight> result = new ArrayList<>();

        for (Flight f : flights) {
            LocalDateTime departureDateTime = f.getDepartureDateTime();
            if (!departureDateTime.isBefore(now)) {
                result.add(f);
            }
        }

        return result;
    }
}
