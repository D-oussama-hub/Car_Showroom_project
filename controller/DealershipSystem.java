package controller;

import database.DatabaseManager;
import model.*;
import java.sql.*;
import java.util.*;

public class DealershipSystem implements IDealershipController {
    
    private static DealershipSystem instance;
    private List<Car> cars;
    private List<Transaction> transactions;
    private List<DealershipObserver> observers;

    private DealershipSystem() {
        cars = new ArrayList<>();
        transactions = new ArrayList<>();
        observers = new ArrayList<>();
        
        DatabaseManager.initializeDatabase();
        loadInventoryFromDB(); 
    }

    public static DealershipSystem getInstance() {
        if (instance == null) {
            instance = new DealershipSystem();
        }
        return instance;
    }

    // --- Authentication & Registration ---
    @Override
    public Person authenticate(String role, String email, String password) {
        // 1. Verify credentials against DB
        if (DatabaseManager.validatePassword(email, password)) {
            // 2. Retrieve User object
            Person user = DatabaseManager.findUserByEmailAndRole(email, role);
            if (user != null) {
                // 3. Auto-attach if it's an observer (Manager/Dealer)
                if (user instanceof DealershipObserver) {
                    attach((DealershipObserver) user);
                }
                return user;
            }
        }
        return null;
    }
    @Override
    public Person registerManager(String name, String phone, String email, String password) {
        Manager m = new Manager((int)(Math.random()*1000), name, phone, email);
        DatabaseManager.saveUser(m, password, "MANAGER");
        attach(m); // Auto-attach observer
        return m;
    }
    @Override
    public Person registerDealer(String name, String phone, String email, String password) {
        Dealer d = new Dealer((int)(Math.random()*1000), name, phone, email, "LIC-NEW");
        DatabaseManager.saveUser(d, password, "DEALER");
        attach(d); // Auto-attach observer
        return d;
    }

    // --- Inventory & Transactions ---

    private void loadInventoryFromDB() {
        String sql = "SELECT vin, brand, model, year, mileage, basePrice, rentPrice, status FROM cars";
        this.cars.clear();
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                boolean status = rs.getInt("status") == 1;
                Car car = new Car(rs.getString("vin"), rs.getString("brand"), rs.getString("model"), 
                                  rs.getInt("year"), rs.getInt("mileage"), rs.getDouble("basePrice"), 
                                  rs.getDouble("rentPrice"), status);
                this.cars.add(car);
            }
            System.out.println("[DB] Loaded " + cars.size() + " cars.");

        } catch (SQLException e) {
            System.out.println("Error loading inventory: " + e.getMessage());
        }
    }
    @Override
    public void addNewCar(Car c) {
        this.cars.add(c);
        saveCarToDB(c);
        System.out.println("[SYSTEM] Inventory updated: " + c.getBrand() + " " + c.getModel());
    }

    public void saveCarToDB(Car c) {
        String sql = "INSERT OR REPLACE INTO cars(vin, brand, model, year, mileage, basePrice, rentPrice, status) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getVin());
            pstmt.setString(2, c.getBrand());
            pstmt.setString(3, c.getModel());
            pstmt.setInt(4, c.getYear());
            pstmt.setInt(5, c.getMileage());
            pstmt.setDouble(6, c.getBasePrice());
            pstmt.setDouble(7, c.getRentPrice());
            pstmt.setInt(8, c.isAvailable() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    @Override
    public Customer findCustomerById(int id) {
        return DatabaseManager.findCustomerById(id);
    }
    @Override
    public Customer createNewCustomer(String name, String phone, String email, String licenseId) {
        // Generate random ID for demo purposes
        int id = (int)(Math.random() * 10000);
        Customer newC = new Customer(id, name, phone, email, licenseId, false);
        DatabaseManager.saveCustomer(newC);
        System.out.println("[SYSTEM] New Customer Created: " + name + " (ID: " + id + ")");
        return newC;
    }
    
    @Override
    public void processTransactionRequest(Transaction t) {
        
        // 0. Pre-Check: Blacklist Validation
        if (t.getCustomer() != null && t.getCustomer().isBlacklisted()) {
            System.out.println("[ERROR] Transaction Denied: Customer is Blacklisted.");
            return;
        }

        // 1. Execute Business Logic (Sets car.available = false in memory)
        // 1-1. MVC BRIDGE: Hand over the system's observers
        for (DealershipObserver o : this.observers) {
            t.attach(o);
        }

        // 1-2. EXECUTE
        t.execute();
        
        // 1-3. NOTIFY
        t.notifyObservers();
        addTransaction(t);
        
        // 2. Persist Car State
        if (t instanceof TransactionBundle) {
            for (Transaction sub : ((TransactionBundle) t).getSubTransactions()) {
                persistCarState(sub.getCar());
            }
        } else {
            persistCarState(t.getCar());
        }
     // NEW: Log sale specifically for the logged-in dealer
        if (activeDealer != null) {
            double commission = t.getCost() * 0.02;
            // 1. Update Database
            DatabaseManager.logSale(activeDealer.getEmail(), t.getTransactionId().toString(), commission);
            
            // 2. Update Memory (The object the dealer is using right now)
            activeDealer.addToLog(t); 
            
            System.out.println("[SYSTEM] Commission updated for " + activeDealer.getFullName());
        }
        // 3. Persist Transaction Log
        saveTransactionLogToDB(t);

        // 4. NEW: Update Customer Stats (Spend & VIP)
        if (t.getCustomer() != null) {
            double amount = t.getCost();
            // DatabaseManager handles the math and VIP check internally
            DatabaseManager.updateCustomerSpendAndVIP(t.getCustomer().getId(), amount);
        }
    }
    private Dealer activeDealer; // Track who is logged in

    public void setActiveDealer(Dealer d) { this.activeDealer = d; }
    
    public List<String> getDealerSalesHistory(String email) {
        return DatabaseManager.getSalesByDealer(email);
    }
    
    private void saveTransactionLogToDB(Transaction t) {
        String sql = "INSERT INTO transactions(id, type, amount, timestamp) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getTransactionId().toString());
            pstmt.setString(2, t.getClass().getSimpleName());
            pstmt.setDouble(3, t.getCost());
            pstmt.setString(4, new java.util.Date().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("History DB Error: " + e.getMessage());
        }
    }

    // Helper method to handle database persistence for a car
    private void persistCarState(Car car) {
        if (car == null) return;

        // 1. Permanent Save: Push changes to SQLite
        // This uses your existing saveCarToDB logic
        saveCarToDB(car);

        // 2. Memory Sync: Update the existing car in the list
        boolean alreadyInList = false;
        for (Car existingCar : cars) {
            if (existingCar.getVin().equalsIgnoreCase(car.getVin())) {
                // Using your existing setter name
                existingCar.setStatus(car.isAvailable()); 
                alreadyInList = true;
                break;
            }
        }

        // 3. New Entry: If it's a trade-in/buy, add it to the active list
        if (!alreadyInList) {
            cars.add(car);
            System.out.println("[SYSTEM] New car added to inventory: " + car.getVin());
        }
    }
    
 // Add this to DealershipSystem.java
    @Override
    public Car findCarByVin(String vin) {
        for (Car c : cars) {
            if (c.getVin().equalsIgnoreCase(vin)) {
                return c;
            }
        }
        return null; // Not found
    }
    

    

    public void addTransaction(Transaction t) { transactions.add(t); }
    public List<Car> getInventory() { return cars; }
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
}