package model;

public class Customer extends Person {
    private String licenseId;
    private double totalSpend;
    private boolean isBlacklisted;
    private boolean isVIP;

    public Customer(int id, String fullName, String phoneNumber, String email, String licenseId, boolean isVIP) {
        super(id, fullName, phoneNumber, email);
        this.licenseId = licenseId;
        this.isVIP = isVIP;
        this.totalSpend = 0.0;
        this.isBlacklisted = false;
    }

    public boolean isVIP() { return isVIP; }
    
    // Updates status based on total spend
    public void checkAndSetVIP() { 
        if (this.totalSpend > 50000) this.isVIP = true; 
    }

    public double getTotalSpend() { return totalSpend; }
    public void setTotalSpend(double totalSpend) { this.totalSpend = totalSpend; }
    
    public String getLicenseId() { return licenseId; }
    
    public boolean isBlacklisted() { return isBlacklisted; }
    public void setBlacklisted(boolean blacklisted) { isBlacklisted = blacklisted; }
}