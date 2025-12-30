package view;



import java.util.List;
import java.util.Scanner;

import controller.DealershipSystem;
import model.*;


public class ManagerView {
    
    public void displayDashboard() {
        System.out.println("\n--- MANAGER DASHBOARD ---");
        System.out.println("System operational. Waiting for updates...");
    }
    
 // Add to view/ManagerView.java
    public void addNewCarSubmitWithScanner(Scanner scanner) {
        DealershipSystem system = DealershipSystem.getInstance();
        
        try {
            System.out.println("\n--- ADD NEW CAR ---");
            
            // 1. Unique VIN Validation Loop
            String vin;
            while (true) {
                System.out.print("VIN: ");
                vin = scanner.nextLine();
                
                if (vin.trim().isEmpty()) {
                    System.out.println("Error: VIN cannot be empty.");
                    continue;
                }

                // Check if VIN already exists in the system
                if (system.findCarByVin(vin) != null) {
                    System.out.println("Error: A car with VIN [" + vin + "] already exists in the database.");
                    System.out.println("Please enter a different VIN.");
                } else {
                    break; // VIN is unique, exit the loop
                }
            }

            // 2. Collect remaining details
            System.out.print("Brand: "); String brand = scanner.nextLine();
            System.out.print("Model: "); String model = scanner.nextLine();
            System.out.print("Year: "); int year = Integer.parseInt(scanner.nextLine());
            System.out.print("Mileage: "); int mileage = Integer.parseInt(scanner.nextLine());
            System.out.print("Base Price: "); double bPrice = Double.parseDouble(scanner.nextLine());
            System.out.print("Rent Price: "); double rPrice = Double.parseDouble(scanner.nextLine());

            // 3. Create and Save
            Car c = new Car(vin, brand, model, year, mileage, bPrice, rPrice, true);
            system.addNewCar(c);
            
            System.out.println("Success: " + brand + " " + model + " added to inventory.");
            
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format for Year, Mileage, or Price. Operation cancelled.");
        }
    }

    public void onAuthorizeClick(Transaction t) {
        System.out.println("Manager authorizing transaction: " + t.getTransactionId());
        // Call controller logic if needed
    }

    public void displayDealerLogs(Scanner scanner) {
        System.out.print("Enter Dealer Email to search: ");
        String email = scanner.nextLine();
        List<String> logs = DealershipSystem.getInstance().getDealerSalesHistory(email);
        
        System.out.println("\n--- SALES LOG FOR: " + email + " ---");
        if (logs.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            logs.forEach(System.out::println);
        }
    }
    
    
    public void showAlert(String msg) {
        System.out.println("[ALERT] " + msg);
    }
}

