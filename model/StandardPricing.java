package model;

public class StandardPricing implements PricingStrategy {
    @Override
    public double calculate(double basePrice) { return basePrice; }
    @Override
    public double calculateRent(double rentPrice) { return rentPrice; }
}
