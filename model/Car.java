package model;

public class Car {
    private String vin;
    private String brand;
    private String model;
    private int year;
    private int mileage;
    private double basePrice;
    private double rentPrice;
    private boolean status; // true = available

    public Car(String vin, String brand, String model, int year, int mileage, double basePrice, double rentPrice, boolean status) {
        this.vin = vin;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.mileage = mileage;
        this.basePrice = basePrice;
        this.rentPrice = rentPrice;
        this.status = status;
    }

    public boolean isAvailable() {
        return this.status; 
    }
    public void setStatus(boolean status) { this.status = status; }
    
    // Getters
    public String getVin() { return vin; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public int getMileage() { return mileage; }
    public double getBasePrice() { return basePrice; }
    public double getRentPrice() { return rentPrice; }

    @Override
    public String toString() {
        return brand + " " + model + " (" + year + ") [VIN:" + vin + "]";
    }
}



