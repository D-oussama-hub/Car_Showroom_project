package model;

 public class Sell extends Transaction {
    private Car car;

    public Sell(Car car) {
        this.car = car;
    }

    @Override
    public void execute() {
        if(car.isAvailable()) {
            car.setStatus(false);
            this.totalFinalPrice = strategy.calculate(car.getBasePrice());
            System.out.println("EXECUTING SELL: " + car.toString() + " | Final Price: " + this.totalFinalPrice);
        } else {
            System.out.println("ERROR: Car " + car.getVin() + " is not available!");
        }
    }
    
    @Override
    public Car getCar() {
        return this.car; // Now the controller can see the car!
    }

    @Override
    public double getCost() {
        // Recalculate based on current strategy if not executed, or return final
        return strategy.calculate(car.getBasePrice());
    }
}