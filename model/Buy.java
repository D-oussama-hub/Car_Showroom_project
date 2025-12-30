package model;

public class Buy extends Transaction {
    private Car car;
    private String condition;

    public Buy(Car car, String condition) {
        this.car = car;
        this.condition = condition;
    }

    @Override
    public void execute() {
        // Dealership buying a car -> Add to inventory (handled by controller usually, but logical state update here)
        car.setStatus(true); 
        this.totalFinalPrice = car.getBasePrice(); // Buying price
        System.out.println("EXECUTING BUY: Dealer bought " + car.toString() + " (" + condition + ")");
    }
    
    @Override
    public Car getCar() {
        return this.car; // Now the controller can see the car!
    }

    @Override
    public double getCost() {
        // Negative cost because money is leaving the dealership
        return -car.getBasePrice();
    }
}