package airlinesystem.concurrent;

import airlinesystem.model.Flight;
import airlinesystem.model.Reservation;
import airlinesystem.service.FlightManager;
import airlinesystem.service.ReservationManager;

import java.util.List;

/**
 * Uzun süren rapor hesaplamasını (örneğin tüm uçuşların doluluk oranı)
 * ana GUI thread'ini bloklamadan yapmak için ayrı bir thread'te çalıştırılan sınıf.
 */
public class ReportGenerator implements Runnable {

    private final FlightManager flightManager;
    private final ReservationManager reservationManager;
    private final ReportListener listener;
    private final long artificialDelayMillis;

    /**
     * @param flightManager      Tüm uçuşlara erişmek için.
     * @param reservationManager Rezervasyonlara erişmek için.
     * @param listener           Rapor hazır olduğunda çağrılacak callback.
     * @param artificialDelayMillis Hesaplama sırasında bekleme süresi (GUI'de "uzun işlem" simüle etmek için).
     */
    public ReportGenerator(FlightManager flightManager,
                           ReservationManager reservationManager,
                           ReportListener listener,
                           long artificialDelayMillis) {
        if (flightManager == null || reservationManager == null || listener == null) {
            throw new IllegalArgumentException("flightManager, reservationManager ve listener null olamaz");
        }
        this.flightManager = flightManager;
        this.reservationManager = reservationManager;
        this.listener = listener;
        this.artificialDelayMillis = artificialDelayMillis;
    }

    @Override
    public void run() {
        try {
            // Uzun süren işlem simülasyonu
            if (artificialDelayMillis > 0) {
                Thread.sleep(artificialDelayMillis);
            }

            String report = generateOccupancyReport();

            // Rapor hazır; GUI'ye / dinleyiciye haber ver
            listener.onReportReady(report);

        } catch (InterruptedException e) {
            // Gerçek sistemde logging yapılır; burada basitçe rapor veriyoruz
            listener.onReportReady("Report generation was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Her bir uçuş için:
     * - kapasite
     * - aktif rezervasyon sayısı
     * - doluluk oranı (%)
     * hesaplayıp String rapor döner.
     */
    private String generateOccupancyReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Flight Occupancy Report ===\n");

        List<Flight> flights = flightManager.getFlights();
        List<Reservation> reservations = reservationManager.getReservations();

        if (flights.isEmpty()) {
            sb.append("No flights found.\n");
            return sb.toString();
        }

        for (Flight f : flights) {
            int capacity = f.getFlightPlane().getPlaneCapacity();
            int reservedCount = 0;

            for (Reservation r : reservations) {
                if (r.isActive() && r.getFlight() == f) {
                    reservedCount++;
                }
            }

            double occupancy = capacity == 0
                    ? 0.0
                    : reservedCount * 100.0 / capacity;

            sb.append(String.format(
                    "Flight %s (%s -> %s) : %d / %d (%.2f%%)\n",
                    f.getFlightNum(),
                    f.getFlightRoute().getDeparturePlace(),
                    f.getFlightRoute().getArrivalPlace(),
                    reservedCount,
                    capacity,
                    occupancy
            ));
        }

        return sb.toString();
    }
}
