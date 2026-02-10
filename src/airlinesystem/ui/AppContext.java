package airlinesystem.ui;

import airlinesystem.repository.PassengerFileRepository;
import airlinesystem.repository.StaffFileRepository;
import airlinesystem.repository.PlaneFileRepository;
import airlinesystem.repository.FlightFileRepository;
import airlinesystem.repository.ReservationFileRepository;
import airlinesystem.service.*;
import airlinesystem.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uygulamada kullanılacak ortak servisleri ve repository'leri tutar.
 * Bunu GUI pencerelerine geçerek aynı nesneleri paylaşacağız.
 */
public class AppContext {

    private final SeatManager seatManager;
    private final PriceCalculator priceCalculator;
    private final ReservationManager reservationManager;
    private final FlightManager flightManager;

    private final PassengerFileRepository passengerRepo;
    private final StaffFileRepository staffRepo;
    private final PlaneFileRepository planeRepo;
    private final FlightFileRepository flightRepo;
    private final ReservationFileRepository reservationRepo;

    // FlightManager'e flight eklerken admin kontrolü var,
    // o yüzden içeride kullanılan "sistem admin" tanımlıyoruz.
    private final Staff systemAdmin;

    public AppContext() {
        this.seatManager     = new SeatManager();
        this.priceCalculator = new BasicPriceCalculator(1.5, 10.0);
        this.flightManager   = new FlightManager();

        this.passengerRepo   = new PassengerFileRepository("users.txt");
        this.staffRepo       = new StaffFileRepository("staff.txt");
        this.planeRepo       = new PlaneFileRepository("plane.txt");
        this.flightRepo      = new FlightFileRepository("flights.txt");
        this.reservationRepo = new ReservationFileRepository("reservations.txt");

        // ReservationManager artık repo ile birlikte çalışıyor
        this.reservationManager =
                new ReservationManager(seatManager, priceCalculator, reservationRepo);

        // Sadece dosyadan load için kullanılacak gizli admin
        this.systemAdmin = new Staff(
                "SYS-ADMIN",
                "System",
                "Admin",
                "sysadmin@example.com",
                "sysadmin",
                "sysadmin",
                "ADMIN"
        );

        // Kullanıcıları yükle
        try {
            passengerRepo.loadAll();
        } catch (IOException e) {
            System.err.println("users.txt yüklenirken hata: " + e.getMessage());
        }

        try {
            staffRepo.loadAll();
        } catch (IOException e) {
            System.err.println("staff.txt yüklenirken hata: " + e.getMessage());
        }

        // 1) Uçak + uçuşları yükle (ve koltukları oluştur)
        loadInitialFlights();

        // 2) Rezervasyonları (ve biletleri) yükle
        loadInitialReservations();
    }

    /**
     * Program açılırken plane.txt + flights.txt içindeki verileri
     * FlightManager'e yükler ve her flight için koltukları oluşturur.
     */
    private void loadInitialFlights() {
        try {
            // 1) plane.txt'den uçakları al
            List<Plane> planes = planeRepo.loadAll();
            Map<String, Plane> planeById = new HashMap<>();
            for (Plane p : planes) {
                planeById.put(p.getPlaneID(), p);
            }

            // 2) flights.txt'den uçuşları al
            List<Flight> loadedFlights = flightRepo.loadAll(planeById);

            // 3) FlightManager'e ekle (admin zorunluluğu yüzünden systemAdmin ile)
            for (Flight f : loadedFlights) {
                try {
                    flightManager.addFlight(systemAdmin, f);

                    int defaultBusinessRows = 2;
                    double basePrice = f.getEconomyBasePrice();
                    seatManager.createSeats(
                            f.getFlightPlane(),
                            defaultBusinessRows,
                            basePrice,
                            1.5
                    );

                } catch (Exception ex) {
                    System.err.println("Uçuş yüklenirken hata ("
                            + f.getFlightNum() + "): " + ex.getMessage());
                }
            }

            System.out.println("Başlangıçta yüklenen flight sayısı: " + loadedFlights.size());
        } catch (IOException e) {
            System.err.println("flights.txt / plane.txt yüklenirken hata: " + e.getMessage());
        }
    }

    /**
     * Program açılırken reservations.txt içindeki rezervasyon + biletleri
     * ReservationManager'a ve koltuk durumuna yükler.
     */
    private void loadInitialReservations() {
        try {
            reservationRepo.loadAll(
                    reservationManager,
                    flightManager,
                    passengerRepo,
                    seatManager
            );
        } catch (IOException e) {
            System.err.println("reservations.txt yüklenirken hata: " + e.getMessage());
        }
    }

    public SeatManager getSeatManager() {
        return seatManager;
    }

    public PriceCalculator getPriceCalculator() {
        return priceCalculator;
    }

    public ReservationManager getReservationManager() {
        return reservationManager;
    }

    public FlightManager getFlightManager() {
        return flightManager;
    }

    public PassengerFileRepository getPassengerRepo() {
        return passengerRepo;
    }

    public StaffFileRepository getStaffRepo() {
        return staffRepo;
    }

    public PlaneFileRepository getPlaneRepo() {
        return planeRepo;
    }

    public FlightFileRepository getFlightRepo() {
        return flightRepo;
    }

    public ReservationFileRepository getReservationRepo() {
        return reservationRepo;
    }

    public Staff getSystemAdmin() {
        return systemAdmin;
    }
}
