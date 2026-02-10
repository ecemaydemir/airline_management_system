package airlinesystem.service;

import airlinesystem.model.Flight;
import airlinesystem.model.Seat;
import airlinesystem.model.Baggage;

public interface PriceCalculator {

    double calculatePrice(Flight flight,
                          Seat seat,
                          double baggageAllowance,
                          Baggage baggage);

    double getSeatClassMultiplier(airlinesystem.model.SeatClass seatClass);

    double calculateBaggageCost(double allowance, Baggage baggage);
}
