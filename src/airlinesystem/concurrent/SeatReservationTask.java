package airlinesystem.concurrent;

import airlinesystem.model.Plane;
import airlinesystem.model.Seat;
import airlinesystem.service.SeatManager;

import java.util.Random;

public class SeatReservationTask implements Runnable {

    private final Plane plane;
    private final SeatManager seatManager;
    private final boolean synchronizedMode;
    private final Object lock;
    private final Random random = new Random();

    public SeatReservationTask(Plane plane,
                               SeatManager seatManager,
                               boolean synchronizedMode,
                               Object lock) {
        this.plane = plane;
        this.seatManager = seatManager;
        this.synchronizedMode = synchronizedMode;
        this.lock = lock;
    }

    @Override
    public void run() {
        while (true) {
            int row = random.nextInt(plane.getRows());
            int col = random.nextInt(plane.getColumns());
            Seat seat = plane.getSeat(row, col);

            if (seat == null) continue;

            if (synchronizedMode) {
                synchronized (lock) {
                    if (!seat.isReserved()) {
                        try {
                            Thread.sleep(5); // KRİTİK BÖLGEYİ UZAT
                        } catch (InterruptedException ignored) {}

                        seat.makeReservation();
                        break;
                    }
                }
            } else {
                // SENKRON OLMAYAN MOD
                try {
                    if (!seat.isReserved()) {

                        Thread.sleep(5); // BURADA YARIŞMA OLUŞSUN

                        seat.makeReservation();
                        break;
                    }
                } catch (Exception ignored) {
                    break;
                }
            }
        }
    }

}
