package airlinesystem.model;

public class Baggage {
    private double weight;  // kilograms

    public Baggage(double weight) {
        setWeight(weight);
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Baggage weight cannot be negative");
        }
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Baggage{weight=" + weight + " kg}";
    }
}
