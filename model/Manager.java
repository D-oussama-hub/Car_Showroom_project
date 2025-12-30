package model;

import controller.DealershipSystem;
import java.util.Scanner;

public class Manager extends Person implements DealershipObserver {
    public Manager(int id, String name, String phone, String email) {
        super(id, name, phone, email);
    }
    
    Scanner scanner = new Scanner(System.in);

    @Override
    public void update(Transaction t) {
        System.out.println("\n[MANAGER ALERT] " + fullName + " notified: New Transaction " + t.getTransactionId());
        System.out.println("Details: Cost " + t.getCost());
    }

    
    public boolean authorizeTransaction(Transaction t) {
        // Logic: Managers reject transactions over 50k automatically for this demo
        return t.getCost() < 50000;
    }
}