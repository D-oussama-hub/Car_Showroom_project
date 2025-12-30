package model;


public interface Subject {
    void attach(DealershipObserver o);
    void detach(DealershipObserver o);
    void notifyObservers();
}