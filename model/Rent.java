package model;

public class Rent extends Transaction {
    private Car car;
    private int days;

    public Rent(Car car, int days) {
        this.car = car;
        this.days = days;
    }

    @Override
    public void execute() {
        this.totalFinalPrice = strategy.calculateRent(car.getRentPrice()) * days;
        System.out.println("EXECUTING RENT: " + car.toString() + " for " + days + " days. Total: " + totalFinalPrice);
    }
    
    @Override
    public Car getCar() {
        return this.car; // Now the controller can see the car!
    }

    @Override
    public double getCost() {
        return strategy.calculateRent(car.getRentPrice()) * days;
    }
}
