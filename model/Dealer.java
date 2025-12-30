package model;

import java.util.ArrayList;
import java.util.List;

public class Dealer extends Person implements DealershipObserver {
    private double currentCommissionTotal;
    private List<Transaction> salesLog;
    private String licenseNumber; // Added to match the DB/Business logic

    // Updated constructor to include licenseNumber
    public Dealer(int id, String name, String phone, String email, String licenseNumber) {
        super(id, name, phone, email);
        this.licenseNumber = licenseNumber;
        this.salesLog = new ArrayList<>();
        this.currentCommissionTotal = 0;
    }

    @Override
    public void update(Transaction t) {
        // Logic: Only the dealer who actually handled the sale should update their commission in DB
        // We check this in the Controller, but the Dealer class remains the Observer
        System.out.println("[NOTIFY] Dealer " + fullName + " received system update for transaction " + t.getTransactionId());
    }
    
    public void addToLog(Transaction t) {
        salesLog.add(t);
        // Increment the total in memory by 2% of this transaction
        this.currentCommissionTotal += (t.getCost() * 0.02);
    }
    
    
    // --- Getters and Setters ---
    
    public void setCommissionTotal(double commission) {
        this.currentCommissionTotal = commission;
    }
    
    public String getLicenseNumber() {
        return licenseNumber;
    }

    public double getCommission() { 
        return currentCommissionTotal; 
    }

 // Inside Dealer.java
    @Override
    public String toString() {
        // We can't easily reach the DB from the Model, so we assume the 
        // Controller/DatabaseManager set this value when the Dealer was created/loaded.
        return String.format("Dealer: %s | License: %s | Total Commission: $%.2f", 
                              fullName, licenseNumber, currentCommissionTotal);
    }
}