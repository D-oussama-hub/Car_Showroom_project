package main;

import controller.DealershipSystem;
import model.*;
import view.*;
import java.util.List;
import java.util.Scanner;

public class Main1 {
    private static Scanner scanner = new Scanner(System.in);
    private static DealershipSystem system = DealershipSystem.getInstance();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n--- Welcome to Dealership CLI ---");
            System.out.println("1) Login");
            System.out.println("2) Create Account");
            System.out.println("3) Exit");
            System.out.print("> ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegistration();
                    break;
                case "3":
                    System.out.println("Goodbye.");
                    System.exit(0);
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void handleLogin() {
        System.out.print("Role (MANAGER/DEALER): ");
        String role = scanner.nextLine().toUpperCase();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        Person user = system.authenticate(role, email, pass);

     // Inside handleLogin() in Main1.java
        if (user != null) {
            System.out.println("Login successful! Welcome, " + user.getFullName());
            if (user instanceof Manager) {
                managerLoop((Manager) user);
            } else if (user instanceof Dealer) {
                // TELL THE SYSTEM WHO IS LOGGED IN
                system.setActiveDealer((Dealer) user); 
                dealerLoop((Dealer) user);
                // CLEAR SESSION ON LOGOUT
                system.setActiveDealer(null); 
            }
        } else {
            System.out.println("Authentication failed. Invalid credentials or role.");
        }
    }

    private static void handleRegistration() {
        System.out.print("Register Role (MANAGER/DEALER): ");
        String role = scanner.nextLine().toUpperCase();
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        if (role.equals("MANAGER")) {
            system.registerManager(name, phone, email, pass);
            System.out.println("Manager registered successfully.");
        } else if (role.equals("DEALER")) {
            system.registerDealer(name, phone, email, pass);
            System.out.println("Dealer registered successfully.");
        } else {
            System.out.println("Invalid role selected.");
        }
    }

    private static void managerLoop(Manager m) {
        ManagerView view = new ManagerView(); // Assumes ManagerView exists
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- MANAGER MENU ---");
            System.out.println("1) Add New Car");
            System.out.println("2) View Inventory");
            System.out.println("3) display Dealer Logs");
            System.out.println("4) Logout");
            System.out.print("> ");
            
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    // IMPORTANT: You must modify ManagerView to accept a Scanner 
                    // or use the logic below to handle it here if View isn't changed.
                    // Assuming ManagerView has method updated: addNewCarSubmit(Scanner sc)
                    // If not, use the View's existing method but ensure it doesn't close System.in
                    view.addNewCarSubmitWithScanner(scanner); 
                    break;
                case "2":
                    printInventory();
                    break;
                case "3":
                    view.displayDealerLogs(scanner);
                    break;
                case "4":
                    loggedIn = false; // Move logout to 4
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void dealerLoop(Dealer d) {
        DealerView view = new DealerView();
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- DEALER MENU ---");
            System.out.println("\n--- " + d.toString() + " ---");
            System.out.println("1) View Inventory");
            System.out.println("2) Submit Transaction");
            System.out.println("3) Logout");
            System.out.print("> ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    view.renderCarInventory(system.getInventory());
                    break;
                case "2":
                    System.out.println("\n--- NEW TRANSACTION ---");
                    System.out.println("Choose Transaction Type:");
                    System.out.println("1) Sell (Standard)");
                    System.out.println("2) Rent");
                    System.out.println("3) Trade-In (Transaction Bundle)");
                    System.out.println("4) Buy (Acquire from Customer)");
                    System.out.print("> ");
                    String transType = scanner.nextLine();

                    switch (transType) {
                    case "1":
                        // The View now handles car selection, customer identification, and processing
                        view.handleSaleFlow(scanner);
                        break;

                    case "2":
                        // The View handles car selection, rental days, and customer logic
                        view.handleRentFlow(scanner);
                        break;

                    case "3":
                        // The View handles both the car being sold AND the details of the trade-in vehicle
                        view.handleTradeInFlow(scanner);
                        break;
                    case "4":
                    	view.handleBuyFlow(scanner); break;

                    default:
                        System.out.println("Invalid transaction type.");
                        break;
                }
                    break;
                case "3":
                    loggedIn = false;
                    System.out.println("Logged out.");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void printInventory() {
        List<Car> list = system.getInventory();
        System.out.println("\n--- CURRENT INVENTORY ---");
        for (Car c : list) {
            System.out.println(c.toString() + " | $" + c.getBasePrice() + " | Avail: " + c.isAvailable());
        }
    }
}