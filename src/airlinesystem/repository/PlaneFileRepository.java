package airlinesystem.repository;

import airlinesystem.model.Plane;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PlaneFileRepository {

    private final Path filePath;

    public PlaneFileRepository(String fileName) {
        this.filePath = Path.of(fileName);
    }

    public List<Plane> loadAll() throws IOException {
        List<Plane> planes = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return planes; // dosya yoksa boş liste
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(";");
                if (parts.length != 4) {
                    // format bozuksa atlıyoruz
                    continue;
                }

                String planeID = parts[0];
                String model = parts[1];
                int rows = Integer.parseInt(parts[2]);
                int cols = Integer.parseInt(parts[3]);

                Plane plane = new Plane(planeID, model, rows, cols);
                planes.add(plane);
            }
        }
        return planes;
    }

    public void saveAll(List<Plane> planes) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            for (Plane p : planes) {
                String line = String.join(";",
                        p.getPlaneID(),
                        p.getPlaneModel(),
                        String.valueOf(p.getRows()),
                        String.valueOf(p.getColumns())
                );
                bw.write(line);
                bw.newLine();
            }
        }
    }
}
