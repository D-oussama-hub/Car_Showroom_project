package model;

import java.util.ArrayList;
import java.util.List;

public class TransactionBundle extends Transaction {
    private List<Transaction> subTransactions;

    public TransactionBundle() {
        this.subTransactions = new ArrayList<>();
    }

    public void add(Transaction t) { subTransactions.add(t); }
    public void remove(Transaction t) { subTransactions.remove(t); }

    @Override
    public void execute() {
        System.out.println("--- EXECUTING BUNDLE ---");
        for (Transaction t : subTransactions) {
            t.setStrategy(this.strategy);
            t.execute();
            // Note: We do NOT call t.notifyObservers() here 
            // because we don't want to spam the manager.
        }
        
        // Notify the manager once the WHOLE bundle is done
        this.notifyObservers(); 
        System.out.println("--- BUNDLE COMPLETE ---");
    }
    
    public List<Transaction> getSubTransactions() {
        return subTransactions;
    }

    @Override
    public Car getCar() {
        return null; // A bundle is not a single car
    }

    @Override
    public double getCost() {
        double total = 0;
        for (Transaction t : subTransactions) {
            t.setStrategy(this.strategy);
            total += t.getCost();
        }
        return total;
    }
}
