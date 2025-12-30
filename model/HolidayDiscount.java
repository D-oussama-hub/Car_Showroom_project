package model;

public class HolidayDiscount implements PricingStrategy {
    @Override
    public double calculate(double basePrice) { return basePrice * 0.90; } // 10% off
    @Override
    public double calculateRent(double rentPrice) { return rentPrice * 0.95; } // 5% off
}