package database;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:dealership1.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
    	String sqlSales = "CREATE TABLE IF NOT EXISTS sales_log ("
                + " dealer_email text, transaction_id text, "
                + " FOREIGN KEY(dealer_email) REFERENCES users(email), "
                + " FOREIGN KEY(transaction_id) REFERENCES transactions(id));";
    	
    	String sqlCustomers = "CREATE TABLE IF NOT EXISTS customers ("
                + " id integer PRIMARY KEY, name text, phone text, email text, "
                + " licenseId text, totalSpend real, isVIP integer, isBlacklisted integer);";
        // Cars table
        String sqlCars = "CREATE TABLE IF NOT EXISTS cars ("
                + " vin text PRIMARY KEY, brand text, model text, year integer, "
                + " mileage integer, basePrice real, rentPrice real, status integer);";

        // Transactions table
        String sqlTrans = "CREATE TABLE IF NOT EXISTS transactions ("
                + " id text PRIMARY KEY, type text, amount real, timestamp text);";

        // Users table (simplified for demo: generic person fields + role + password)
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users ("
                + " email text PRIMARY KEY, name text, phone text, "
                + " password text, role text, id integer, commission real DEFAULT 0.0);";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
        	stmt.execute(sqlSales);
        	stmt.execute(sqlCustomers);
            stmt.execute(sqlCars);
            stmt.execute(sqlTrans);
            stmt.execute(sqlUsers);
            
            // Seed Default Users if empty
            seedDefaultUsers(conn);
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void saveCustomer(Customer c) {
        String sql = "INSERT OR REPLACE INTO customers(id, name, phone, email, licenseId, totalSpend, isVIP, isBlacklisted) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, c.getId());
            pstmt.setString(2, c.getFullName());
            pstmt.setString(3, c.getPhoneNumber());
            pstmt.setString(4, c.getEmail());
            pstmt.setString(5, c.getLicenseId());
            pstmt.setDouble(6, c.getTotalSpend());
            pstmt.setInt(7, c.isVIP() ? 1 : 0);
            pstmt.setInt(8, c.isBlacklisted() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving customer: " + e.getMessage());
        }
    }

    public static Customer findCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Customer c = new Customer(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("licenseId"),
                    rs.getInt("isVIP") == 1
                );
                // Set internal fields that aren't in constructor
                c.setTotalSpend(rs.getDouble("totalSpend"));
                if (rs.getInt("isBlacklisted") == 1) c.setBlacklisted(true);
                return c;
            }
        } catch (SQLException e) {
            System.out.println("Error finding customer: " + e.getMessage());
        }
        return null;
    }

    public static void updateCustomerSpendAndVIP(int customerId, double amountToAdd) {
        // 1. Get current data
        Customer c = findCustomerById(customerId);
        if (c == null) return;

        // 2. Update logic
        double newTotal = c.getTotalSpend() + amountToAdd;
        boolean newVipStatus = c.isVIP();
        
        // Check VIP threshold (Business Rule > 50,000)
        if (newTotal > 50000) {
            newVipStatus = true; 
        }

        // 3. Push to DB
        String sql = "UPDATE customers SET totalSpend = ?, isVIP = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newTotal);
            pstmt.setInt(2, newVipStatus ? 1 : 0);
            pstmt.setInt(3, customerId);
            pstmt.executeUpdate();
            System.out.println("[DB] Customer " + customerId + " spend updated. Total: " + newTotal + " (VIP: " + newVipStatus + ")");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void logSale(String dealerEmail, String transactionId, double commissionEarned) {
        String sqlInsert = "INSERT INTO sales_log(dealer_email, transaction_id) VALUES(?,?)";
        String sqlUpdateDealer = "UPDATE users SET commission = commission + ? WHERE email = ?";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false); // Transactional integrity
            
            try (PreparedStatement ps1 = conn.prepareStatement(sqlInsert);
                 PreparedStatement ps2 = conn.prepareStatement(sqlUpdateDealer)) {
                
                ps1.setString(1, dealerEmail);
                ps1.setString(2, transactionId);
                ps1.executeUpdate();

                ps2.setDouble(1, commissionEarned);
                ps2.setString(2, dealerEmail);
                ps2.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error logging sale: " + e.getMessage());
        }
    }
    
    public static List<String> getSalesByDealer(String email) {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT t.type, t.amount, t.timestamp FROM transactions t " +
                     "JOIN sales_log sl ON t.id = sl.transaction_id WHERE sl.dealer_email = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(rs.getString("timestamp") + " | " + rs.getString("type") + " | Total: $" + rs.getDouble("amount"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return logs;
    }
    
    private static void seedDefaultUsers(Connection conn) throws SQLException {
        String countSql = "SELECT count(*) FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("[DB] Seeding default users...");
                // Seed Manager Alice
                createUser(conn, "Alice Boss", "555-0100", "alice@dealer.com", "alicepass", "MANAGER", 1);
                // Seed Dealer Bob
                createUser(conn, "Bob Sales", "555-0101", "bob@dealer.com", "bobpass", "DEALER", 2);
            }
        }
    }

    private static void createUser(Connection conn, String name, String phone, String email, String password, String role, int id) throws SQLException {
        String sql = "INSERT INTO users(email, name, phone, password, role, id) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, name);
            pstmt.setString(3, phone);
            pstmt.setString(4, password);
            pstmt.setString(5, role);
            pstmt.setInt(6, id);
            pstmt.executeUpdate();
        }
    }

    // --- Public Methods for Controller ---

    public static void saveUser(Person person, String password, String role) {
        String sql = "INSERT OR REPLACE INTO users(email, name, phone, password, role, id) VALUES(?,?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, person.getEmail()); // Email is PK
            pstmt.setString(2, person.getFullName());
            pstmt.setString(3, person.getPhoneNumber());
            pstmt.setString(4, password);
            pstmt.setString(5, role);
            // In a real app, ID would be auto-generated
            pstmt.setInt(6, (int)(Math.random() * 10000)); 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }

    public static Person findUserByEmailAndRole(String email, String role) {
        // 1. Added 'commission' to the SELECT query
        String sql = "SELECT id, name, phone, commission FROM users WHERE email = ? AND role = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, role);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                // 2. Retrieve the commission value from the result set
                double commission = rs.getDouble("commission");
                
                // Return appropriate subclass based on role
                if ("MANAGER".equalsIgnoreCase(role)) {
                    return new Manager(id, name, phone, email);
                } else if ("DEALER".equalsIgnoreCase(role)) {
                    // 3. Create the Dealer and set their loaded commission
                    Dealer d = new Dealer(id, name, phone, email, "LIC-" + id);
                    d.setCommissionTotal(commission); 
                    return d;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding user: " + e.getMessage());
        }
        return null;
    }
    
    public static boolean validatePassword(String email, String password) {
        String sql = "SELECT password FROM users WHERE email = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password").equals(password);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}