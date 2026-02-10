package airlinesystem.model;

import java.util.Objects;

public class Route {

    private final String departurePlace;
    private final String arrivalPlace;

    public Route(String departurePlace, String arrivalPlace) {

        if (departurePlace == null || departurePlace.isBlank()) {
            throw new IllegalArgumentException("Departure place cannot be null or blank");
        }
        if (arrivalPlace == null || arrivalPlace.isBlank()) {
            throw new IllegalArgumentException("Arrival place cannot be null or blank");
        }
        if (departurePlace.equalsIgnoreCase(arrivalPlace)) {
            throw new IllegalArgumentException("Departure and arrival must be different");
        }

        this.departurePlace = departurePlace.toUpperCase();
        this.arrivalPlace = arrivalPlace.toUpperCase();
    }

    public String getDeparturePlace() {
        return departurePlace;
    }

    public String getArrivalPlace() {
        return arrivalPlace;
    }

    @Override
    public String toString() {
        return departurePlace + " -> " + arrivalPlace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route route = (Route) o;
        return departurePlace.equals(route.departurePlace) &&
               arrivalPlace.equals(route.arrivalPlace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departurePlace, arrivalPlace);
    }
}
