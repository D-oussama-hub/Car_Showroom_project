package model;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public abstract class Transaction implements Subject {
    protected UUID transactionId;
    protected Date timestamp;
    protected double totalFinalPrice;
    protected PricingStrategy strategy;
 // In abstract class Transaction
    protected Customer customer;


 // The Model now holds the state of its observers
    private List<DealershipObserver> observers = new ArrayList<>();

    @Override
    public void attach(DealershipObserver o) { 
        if (o != null && !observers.contains(o)) {
            observers.add(o); 
        }
    }

    @Override
    public void detach(DealershipObserver o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (DealershipObserver o : observers) {
            o.update(this); 
        }
    }

    public Transaction() {
        this.transactionId = UUID.randomUUID();
        this.timestamp = new Date();
        this.strategy = new StandardPricing(); // Default
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return this.customer;
    }
    
    public abstract Car getCar();

    public void setStrategy(PricingStrategy s) { this.strategy = s; }
    public UUID getTransactionId() { return transactionId; }
    
    public abstract void execute();
    public abstract double getCost();
}







