package airlinesystem.unittest;

import airlinesystem.model.Plane;
import airlinesystem.model.Seat;
import airlinesystem.service.SeatManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class SeatManagerTest {

    @Test
    public void emptySeatsCount_decreasesAfterReservation() {
        Plane plane = new Plane("P1", "B737", 5, 4); // 20 koltuk
        SeatManager seatManager = new SeatManager();

        seatManager.createSeats(plane, 2, 1000.0, 1.5);

        int emptyBefore = seatManager.getAvailableSeatCount(plane);

        // Bir koltuk rezerve et
        Seat seat = seatManager.resolveSeatNumber(plane, "3A");
        assertNotNull(seat);
        assertFalse(seat.isReserved());

        seat.makeReservation();

        int emptyAfter = seatManager.getAvailableSeatCount(plane);

        assertEquals(emptyBefore - 1, emptyAfter);
    }

    @Test
    public void reserveSeat_throwsExceptionForInvalidSeat() {
        Plane plane = new Plane("P1", "B737", 5, 4);
        SeatManager seatManager = new SeatManager();
        seatManager.createSeats(plane, 2, 1000.0, 1.5);

        assertThrows(IllegalArgumentException.class, () -> {
            seatManager.reserveSeat(plane, "99Z");
        });
    }
    

}
