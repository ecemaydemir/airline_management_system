package airlinesystem.repository;

import airlinesystem.model.Flight;
import airlinesystem.model.Plane;
import airlinesystem.model.Route;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlightFileRepository {

    private final Path filePath;

    public FlightFileRepository(String fileName) {
        this.filePath = Path.of(fileName);
    }

    /**
     * Dosya formatı (8 alan):
     * flightNum;dep;arr;date;time;duration;planeID;basePrice
     */
    public List<Flight> loadAll(Map<String, Plane> planeById) throws IOException {
        List<Flight> flights = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return flights;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(";");
                if (parts.length != 8) {
                    continue;
                }

                String flightNum = parts[0];
                String dep       = parts[1];
                String arr       = parts[2];
                LocalDate date   = LocalDate.parse(parts[3]);
                LocalTime time   = LocalTime.parse(parts[4]);
                int duration     = Integer.parseInt(parts[5]);
                String planeID   = parts[6];
                double basePrice = Double.parseDouble(parts[7]);

                Plane plane = planeById.get(planeID);
                if (plane == null) {
                    // Plane bulunamazsa bu flight'ı atla
                    continue;
                }

                Route route = new Route(dep, arr);
                Flight flight = new Flight(flightNum, route, plane, date, time, duration, basePrice);
                flights.add(flight);
            }
        }

        return flights;
    }

    public void saveAll(List<Flight> flights) throws IOException {
        // Gerekirse klasörü oluştur
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            for (Flight f : flights) {
                Route route = f.getFlightRoute();

                String line = String.join(";",
                        f.getFlightNum(),
                        route.getDeparturePlace(),
                        route.getArrivalPlace(),
                        f.getDate().toString(),                // 2025-12-31
                        f.getTime().toString(),                // 14:30
                        String.valueOf(f.getFlightDuration()),
                        f.getFlightPlane().getPlaneID(),
                        String.valueOf(f.getEconomyBasePrice())
                );
                bw.write(line);
                bw.newLine();
            }
        }
    }

}
