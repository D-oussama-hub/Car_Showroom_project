package controller;

import model.*;
import java.util.List;

public interface IDealershipController {
    // Authentication
    Person authenticate(String role, String email, String password);
    Person registerManager(String name, String phone, String email, String password);
    Person registerDealer(String name, String phone, String email, String password);

    // Business Logic
    void processTransactionRequest(Transaction t);
    void addNewCar(Car c);
    
    // Data Access
    List<Car> getInventory();
    Car findCarByVin(String vin);
    Customer findCustomerById(int id);
    Customer createNewCustomer(String name, String phone, String email, String licenseId);

    // Observer Management
    void attach(DealershipObserver o);
    void detach(DealershipObserver o);
}
