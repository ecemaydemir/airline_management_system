package airlinesystem.unittest;

import airlinesystem.model.Flight;
import airlinesystem.model.Plane;
import airlinesystem.model.Route;
import airlinesystem.model.Staff;
import airlinesystem.service.FlightManager;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.*;

public class FlightSearchEngineTest {

    private Staff adminStaff() {
        return new Staff("S001", "Admin", "User", "admin@gmail.com",
                "adminuser", "123456", "ADMIN");
    }

    private Flight createFlight(String flightNo, String from, String to, LocalDateTime depDT) {
        Plane plane = new Plane("P1", "Boeing 737", 5, 4);
        Route route = new Route(from, to);
        return new Flight(
                flightNo,
                route,
                plane,
                depDT.toLocalDate(),
                depDT.toLocalTime(),
                60,
                1000.0
        );
    }

    @Test
    public void searchAvailableFlights_filtersByRouteAndTime() {
        FlightManager fm = new FlightManager();
        Staff admin = adminStaff();

        LocalDateTime now = LocalDateTime.of(2026, 1, 6, 10, 0);

        // IST->ESB future (should match)
        fm.addFlight(admin, createFlight("F001", "IST", "ESB", now.plusHours(2)));

        // IST->ESB past (should NOT match)
        fm.addFlight(admin, createFlight("F002", "IST", "ESB", now.minusHours(2)));

        // different route (should NOT match)
        fm.addFlight(admin, createFlight("F003", "IST", "ADB", now.plusHours(2)));

        List<Flight> result = fm.searchAvailableFlights("IST", "ESB", now);

        assertEquals(1, result.size());
        assertEquals("F001", result.get(0).getFlightNum());
    }

    @Test
    public void filterFutureFlights_eliminatesPastFlights() {
        FlightManager fm = new FlightManager();
        Staff admin = adminStaff();

        LocalDateTime now = LocalDateTime.of(2026, 1, 6, 10, 0);

        fm.addFlight(admin, createFlight("F001", "IST", "ESB", now.plusMinutes(10)));
        fm.addFlight(admin, createFlight("F002", "IST", "ESB", now.minusMinutes(10)));

        List<Flight> result = fm.filterFutureFlights(now);

        assertEquals(1, result.size());
        assertEquals("F001", result.get(0).getFlightNum());
    }

    @Test
    public void searchAvailableFlights_isCaseInsensitiveForCities() {
        FlightManager fm = new FlightManager();
        Staff admin = adminStaff();

        LocalDateTime now = LocalDateTime.of(2026, 1, 6, 10, 0);

        fm.addFlight(admin, createFlight("F001", "IST", "ESB", now.plusHours(1)));

        List<Flight> result = fm.searchAvailableFlights("ist", "esb", now);

        assertEquals(1, result.size());
        assertEquals("F001", result.get(0).getFlightNum());
    }

    @Test(expected = IllegalArgumentException.class)
    public void searchAvailableFlights_throwsIfDepartureOrArrivalNull() {
        FlightManager fm = new FlightManager();
        fm.searchAvailableFlights(null, "ESB", LocalDateTime.now());
    }
    
    @Test
    public void searchAvailableFlights_filtersByDepartureAndArrivalCities() {
        FlightManager fm = new FlightManager();
        Staff admin = new Staff("S001","A","B","x@gmail.com","admin","123456","ADMIN");

        Plane p = new Plane("P1","B737",5,4);

        // doğru rota
        fm.addFlight(admin, new Flight("F001", new Route("IST","ESB"), p,
                LocalDate.now().plusDays(1), LocalTime.of(10,0), 60, 1000));

        // departure doğru, arrival yanlış
        fm.addFlight(admin, new Flight("F002", new Route("IST","ADB"), p,
                LocalDate.now().plusDays(1), LocalTime.of(10,0), 60, 1000));

        // arrival doğru, departure yanlış
        fm.addFlight(admin, new Flight("F003", new Route("ANK","ESB"), p,
                LocalDate.now().plusDays(1), LocalTime.of(10,0), 60, 1000));

        // ikisi de yanlış
        fm.addFlight(admin, new Flight("F004", new Route("ANK","ADB"), p,
                LocalDate.now().plusDays(1), LocalTime.of(10,0), 60, 1000));

        LocalDateTime now = LocalDateTime.now();

        List<Flight> result = fm.searchAvailableFlights("IST", "ESB", now);

        assertEquals(1, result.size());
        assertEquals("F001", result.get(0).getFlightNum());
    }

}
