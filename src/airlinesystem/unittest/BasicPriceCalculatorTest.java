package airlinesystem.unittest;

import airlinesystem.model.Baggage;
import airlinesystem.model.Flight;
import airlinesystem.model.Plane;
import airlinesystem.model.Route;
import airlinesystem.model.Seat;
import airlinesystem.model.SeatClass;
import airlinesystem.service.BasicPriceCalculator;
import airlinesystem.service.PriceCalculator;
import airlinesystem.service.SeatManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class BasicPriceCalculatorTest {

    private Flight createSampleFlight(Plane plane) {
        Route route = new Route("IST", "ESB");
        return new Flight(
                "TK123",
                route,
                plane,
                LocalDate.now().plusDays(1),
                LocalTime.of(12, 0),
                60,
                1000.0
        );
    }

    @Test
    void economySeat_NoExtraBaggage_priceEqualsBase() {
        Plane plane = new Plane("P1", "Boeing 737", 5, 4);
        SeatManager sm = new SeatManager();
        sm.createSeats(plane, 2, 1000.0, 1.5);

        Flight flight = createSampleFlight(plane);

        Seat seat = sm.resolveSeatNumber(plane, "3A");
        assertNotNull(seat);
        assertEquals(SeatClass.ECONOMY, seat.getSeatClass());

        PriceCalculator calc = new BasicPriceCalculator(1.5, 10.0);
        double allowance = 15.0;
        Baggage baggage = new Baggage(10.0);

        double price = calc.calculatePrice(flight, seat, allowance, baggage);
        assertEquals(1000.0, price, 1e-6);
    }

    @Test
    void businessSeat_WithExtraBaggage_priceIncludesMultiplierAndExtra() {
        Plane plane = new Plane("P1", "Boeing 737", 5, 4);
        SeatManager sm = new SeatManager();
        sm.createSeats(plane, 2, 1000.0, 1.5);

        Flight flight = createSampleFlight(plane);

        Seat seat = sm.resolveSeatNumber(plane, "1A");
        assertNotNull(seat);
        assertEquals(SeatClass.BUSINESS, seat.getSeatClass());

        PriceCalculator calc = new BasicPriceCalculator(1.5, 10.0);
        double allowance = 15.0;
        Baggage baggage = new Baggage(20.0); // 5 kg extra

        double price = calc.calculatePrice(flight, seat, allowance, baggage);
        assertEquals(1550.0, price, 1e-6);
    }
}
