package model;

public class VIPDiscount implements PricingStrategy {
    @Override
    public double calculate(double basePrice) { return basePrice * 0.85; } // 15% off
    @Override
    public double calculateRent(double rentPrice) { return rentPrice * 0.85; }
}
