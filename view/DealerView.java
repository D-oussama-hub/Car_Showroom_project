package view;

import controller.DealershipSystem;
import controller.IDealershipController;
import model.*;
import java.util.Scanner;
import java.util.List;

public class DealerView {

    public void renderCarInventory(List<Car> list) {
        System.out.println("\n--- DEALER INVENTORY VIEW ---");
        for(Car c : list) {
            System.out.println(c.toString() + " | Price: $" + c.getBasePrice() + " | Available: " + c.isAvailable());
        }
    }

    // 1. YOUR ORIGINAL PERFECT SALE FLOW
    public void handleSaleFlow(Scanner scanner) {
        IDealershipController controller = DealershipSystem.getInstance();
        System.out.println("\n--- NEW SALE TRANSACTION ---");

        System.out.print("Enter VIN of car to sell: ");
        String vin = scanner.nextLine();
        Car selectedCar = controller.findCarByVin(vin);

        if (selectedCar == null) {
            System.out.println("[ERROR] Car not found with VIN: " + vin);
            return;
        }else {System.out.println("Car Found: " + selectedCar.getBrand() + " " + selectedCar.getModel());}
        if (!selectedCar.isAvailable()) {
            System.out.println("[ERROR] Car is already sold or unavailable.");
            return;
        }

        Customer customer = identifyCustomerFlow(scanner, controller);
        if (customer == null) return;

        Sell sale = new Sell(selectedCar); 
        sale.setCustomer(customer); 

        applyStrategyAndProcess(sale, customer, controller);
    }
    
    public void handleBuyFlow(Scanner scanner) {
        IDealershipController controller = DealershipSystem.getInstance();
        System.out.println("\n--- NEW BUY TRANSACTION (Acquiring Vehicle) ---");

        // 1. Collect Car Details
        System.out.print("Enter VIN of the car to buy: ");
        String vin = scanner.nextLine();
        
        // Safety check: Ensure this car isn't already in our system
        Car existingCar = controller.findCarByVin(vin);
        if (existingCar != null) {
            System.out.println("[ERROR] A car with VIN " + vin + " already exists in inventory.");
            return;
        }

        System.out.print("Enter Brand: ");
        String brand = scanner.nextLine();
        System.out.print("Enter Model: ");
        String model = scanner.nextLine();
        System.out.print("Enter Year: ");
        int year = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter Mileage: ");
        int mileage = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter Purchase Price (Amount Dealer pays): ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Set suggested Daily Rent Price: ");
        double rentPrice = Double.parseDouble(scanner.nextLine());

        // Create the new Car object (available by default)
        Car newCar = new Car(vin, brand, model, year, mileage, price, rentPrice, true);

        // 2. Identify the Customer (The person selling to the dealer)
        Customer customer = identifyCustomerFlow(scanner, controller);
        if (customer == null) return;

        // 3. Create Transaction
        // Assuming Buy class takes (Car car, String description)
        Buy purchase = new Buy(newCar, "Dealer Acquisition from " + customer.getFullName());
        purchase.setCustomer(customer);

        // 4. Apply Strategy and Process
        // Note: Acquisition pricing is usually standard, but we maintain your flow
        applyStrategyAndProcess(purchase, customer, controller);
        
        // Ensure the controller adds the car to the actual inventory list
        controller.addNewCar(newCar);
        System.out.println("[SUCCESS] Car acquired and added to inventory.");
    }

    // 2. NEW MATCHING RENT FLOW
    public void handleRentFlow(Scanner scanner) {
        IDealershipController controller = DealershipSystem.getInstance();
        System.out.println("\n--- NEW RENTAL TRANSACTION ---");

        System.out.print("Enter VIN of car to rent: ");
        String vin = scanner.nextLine();
        Car selectedCar = controller.findCarByVin(vin);

        if (selectedCar == null || !selectedCar.isAvailable()) {
            System.out.println("[ERROR] Car unavailable.");
            return;
        }

        System.out.print("Enter number of rental days: ");
        int days = Integer.parseInt(scanner.nextLine());

        Customer customer = identifyCustomerFlow(scanner, controller);
        if (customer == null) return;

        Rent rent = new Rent(selectedCar, days);
        rent.setCustomer(customer);

        applyStrategyAndProcess(rent, customer, controller);
    }

    // 3. NEW MATCHING TRADE-IN FLOW (Composite Pattern)
    public void handleTradeInFlow(Scanner scanner) {
        IDealershipController controller = DealershipSystem.getInstance();
        System.out.println("\n--- NEW TRADE-IN (BUNDLE) TRANSACTION ---");

        // Part A: The Car being sold
        System.out.print("Enter VIN of the car being SOLD: ");
        Car carToSell = controller.findCarByVin(scanner.nextLine());
        if (carToSell == null || !carToSell.isAvailable()) {
            System.out.println("[ERROR] Car unavailable.");
            return;
        }

        // Part B: Identify Customer
        Customer customer = identifyCustomerFlow(scanner, controller);
        if (customer == null) return;

        // Part C: The Car being bought (Trade-In)
        System.out.println("\n--- ENTER TRADE-IN VEHICLE DETAILS ---");
        System.out.print("Trade-In VIN: ");
        String tVin = scanner.nextLine();
        System.out.print("Brand: ");
        String tBrand = scanner.nextLine();
        System.out.print("Model: ");
        String tModel = scanner.nextLine();
        System.out.print("Year: ");
        int tYear = Integer.parseInt(scanner.nextLine());
        System.out.print("Trade-In Value (Credit): ");
        double tPrice = Double.parseDouble(scanner.nextLine());

        Car tradeInCar = new Car(tVin, tBrand, tModel, tYear, 0, tPrice, 0.0, true);
        controller.addNewCar(tradeInCar); // Adds to inventory first

        // Part D: Create Bundle
        TransactionBundle bundle = new TransactionBundle();
        bundle.add(new Sell(carToSell));
        bundle.add(new Buy(tradeInCar, "Trade-In Arrival"));
        bundle.setCustomer(customer);

        applyStrategyAndProcess(bundle, customer, controller);
    }

    // --- HELPER METHODS TO KEEP FLOWS CLEAN ---

    private Customer identifyCustomerFlow(Scanner scanner, IDealershipController controller) {
        System.out.println("1. Existing Customer | 2. New Customer");
        System.out.print("> ");
        int choice = Integer.parseInt(scanner.nextLine());

        if (choice == 1) {
            System.out.print("Enter Customer ID: ");
            int id = Integer.parseInt(scanner.nextLine());
            Customer c = controller.findCustomerById(id);
            if (c == null) System.out.println("[ERROR] Not found.");
            return c;
        } else {
            System.out.print("Name: "); String n = scanner.nextLine();
            System.out.print("Phone: "); String p = scanner.nextLine();
            System.out.print("Email: "); String e = scanner.nextLine();
            System.out.print("Lic: "); String l = scanner.nextLine();
            return controller.createNewCustomer(n, p, e, l);
        }
    }

    private void applyStrategyAndProcess(Transaction t, Customer c, IDealershipController controller) {
    	if (c.isVIP()) {
            t.setStrategy(new VIPDiscount());
            System.out.println("[INFO] VIP Discount Applied.");
        } 
        else if (HolidayUtils.isHoliday()) {
            // You'll need to create this class (e.g., giving a 10% flat discount)
            t.setStrategy(new HolidayDiscount()); 
            System.out.println("[INFO] Holiday Promotion Applied! Happy Holidays!");
        } 
        else {
            t.setStrategy(new StandardPricing());
        }
        controller.processTransactionRequest(t);
        System.out.println("[SUCCESS] Transaction complete for " + c.getFullName());
    }
}