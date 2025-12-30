package model;

public interface PricingStrategy {
    double calculate(double basePrice);
    double calculateRent(double rentPrice);
}





