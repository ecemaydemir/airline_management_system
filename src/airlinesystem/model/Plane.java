package airlinesystem.model;

import java.util.Objects;

public class Plane {
    private final String planeID;
    private final String planeModel;
    private final int rows;
    private final int columns;
    private final Seat[][] seatMatrix;

    public Plane(String planeID, String planeModel, int rows, int columns) {

        if (planeID == null || planeID.isBlank()) {
            throw new IllegalArgumentException("Plane ID cannot be null or blank");
        }

        if (planeModel == null || planeModel.isBlank()) {
            throw new IllegalArgumentException("Plane model cannot be null or blank");
        }

        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Plane rows and columns must be positive");
        }

        this.planeID = planeID;
        this.planeModel = planeModel;
        this.rows = rows;
        this.columns = columns;

        this.seatMatrix = new Seat[rows][columns];
    }

    public String getPlaneID() {
        return planeID;
    }

    public String getPlaneModel() {
        return planeModel;
    }

    public int getPlaneCapacity() {
        return rows * columns;
    }

    public int getCapacity() {
        return getPlaneCapacity();
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Seat getSeat(int row, int column) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) {
            throw new IndexOutOfBoundsException("Invalid seat coordinates");
        }
        return seatMatrix[row][column];
    }

    public void setSeat(int row, int column, Seat seat) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) {
            throw new IndexOutOfBoundsException("Invalid seat coordinates");
        }
        seatMatrix[row][column] = seat;
    }

    @Override
    public String toString() {
        return "Plane{id='" + planeID + "', model='" + planeModel +
                "', capacity=" + getPlaneCapacity() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plane)) return false;
        Plane plane = (Plane) o;
        return planeID.equals(plane.planeID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planeID);
    }
}
