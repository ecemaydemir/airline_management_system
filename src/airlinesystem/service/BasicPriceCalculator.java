package airlinesystem.service;

import airlinesystem.model.Baggage;
import airlinesystem.model.Flight;
import airlinesystem.model.Seat;
import airlinesystem.model.SeatClass;


public class BasicPriceCalculator implements PriceCalculator {

    private final double businessMultiplier; // Örn: 1.5
    private final double extraFeePerKg;      // Örn: 10 TL/kg

    public BasicPriceCalculator(double businessMultiplier, double extraFeePerKg) {
        if (businessMultiplier <= 0) {
            throw new IllegalArgumentException("businessMultiplier must be positive");
        }
        if (extraFeePerKg < 0) {
            throw new IllegalArgumentException("extraFeePerKg cannot be negative");
        }
        this.businessMultiplier = businessMultiplier;
        this.extraFeePerKg = extraFeePerKg;
    }

    @Override
    public double calculatePrice(Flight flight,
                                 Seat seat,
                                 double baggageAllowance,
                                 Baggage baggage) {

        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null");
        }
        if (seat == null) {
            throw new IllegalArgumentException("Seat cannot be null");
        }
        if (baggageAllowance < 0) {
            throw new IllegalArgumentException("Baggage allowance cannot be negative");
        }

        // 1) Economy baz fiyatı al
        double base = flight.getEconomyBasePrice();

        // 2) Koltuk sınıfına göre çarpan uygula
        double price = base * getSeatClassMultiplier(seat.getSeatClass());

        // 3) Bagaj maliyetini ekle
        price += calculateBaggageCost(baggageAllowance, baggage);

        return price;
    }

    @Override
    public double getSeatClassMultiplier(SeatClass seatClass) {
        if (seatClass == null) {
            throw new IllegalArgumentException("Seat class cannot be null");
        }

        switch (seatClass) {
            case ECONOMY:
                return 1.0;
            case BUSINESS:
                return businessMultiplier;
            default:
                // İleride başka bir sınıf eklersen burada patlasın
                throw new IllegalArgumentException("Unsupported seat class: " + seatClass);
        }
    }

    @Override
    public double calculateBaggageCost(double allowance, Baggage baggage) {
        if (allowance < 0) {
            throw new IllegalArgumentException("Baggage allowance cannot be negative");
        }
        if (baggage == null) {
            return 0.0;
        }

        double extra = baggage.getWeight() - allowance;
        if (extra <= 0) {
            return 0.0;
        }
        return extra * extraFeePerKg;
    }

    public double getBusinessMultiplier() {
        return businessMultiplier;
    }

    public double getExtraFeePerKg() {
        return extraFeePerKg;
    }
}
