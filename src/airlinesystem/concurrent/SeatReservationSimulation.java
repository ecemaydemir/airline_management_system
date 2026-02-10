package airlinesystem.concurrent;

import airlinesystem.model.Plane;
import airlinesystem.model.Seat;
import airlinesystem.service.SeatManager;

import java.util.ArrayList;
import java.util.List;

public class SeatReservationSimulation {

    // SeatVisualizationPanel ile uyumlu:
    // 30 satır * 6 sütun = 180 koltuk
    private static final int ROWS = 30;
    private static final int COLS = 6;
    private static final int PASSENGER_COUNT = 90;

    private final SeatManager seatManager = new SeatManager();

    public static class SimulationResult {
        private final Plane plane;
        private final int reservedCount;
        private final int emptyCount;
        // === YENİ: Her koltuk için dolu/boş bilgisi ===
        // index = row * COLS + col
        private final boolean[] seatOccupied;

        public SimulationResult(Plane plane,
                                int reservedCount,
                                int emptyCount,
                                boolean[] seatOccupied) {
            this.plane = plane;
            this.reservedCount = reservedCount;
            this.emptyCount = emptyCount;
            this.seatOccupied = seatOccupied;
        }

        public Plane getPlane() {
            return plane;
        }

        public int getReservedCount() {
            return reservedCount;
        }

        public int getEmptyCount() {
            return emptyCount;
        }

        // === YENİ GETTER ===
        public boolean[] getSeatOccupied() {
            return seatOccupied;
        }
    }

    public SimulationResult runSimulation(boolean synchronizedMode) throws InterruptedException {
        // 180 koltuklu simülasyon uçağı
        Plane plane = new Plane("SIM", "SimPlane", ROWS, COLS);
        // fiyatlar önemli değil, 0 ver geç
        // businessRows = 0 veriyoruz, tüm koltuklar economy olsun
        seatManager.createSeats(plane, 0, 0.0, 1.0);

        Object lock = new Object();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < PASSENGER_COUNT; i++) {
            Runnable task = new SeatReservationTask(
                    plane,
                    seatManager,
                    synchronizedMode,
                    lock
            );
            Thread t = new Thread(task, "Passenger-" + (i + 1));
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        int total = plane.getPlaneCapacity();
        int available = seatManager.getAvailableSeatCount(plane);
        int reserved = total - available;

        // === YENİ: Koltukların doluluk bilgisi ===
        boolean[] seatOccupied = new boolean[ROWS * COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Seat seat = plane.getSeat(r, c);
                int idx = r * COLS + c;
                if (seat != null) {
                    seatOccupied[idx] = seat.isReserved();
                } else {
                    // seat null ise "boş" kabul edelim
                    seatOccupied[idx] = false;
                }
            }
        }

        return new SimulationResult(plane, reserved, available, seatOccupied);
    }
}
