package model;

/**
 * The base class for all users in the system.
 * Updated to support DB persistence and authentication.
 */
public abstract class Person { // Made abstract as we only instantiate subclasses
    protected int id;
    protected String fullName;
    protected String phoneNumber;
    protected String email;

    public Person(int id, String fullName, String phoneNumber, String email) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // --- Getters required by DatabaseManager and Controller ---

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    // Optional: Useful for CLI greetings
    @Override
    public String toString() {
        return String.format("[%d] %s (%s)", id, fullName, email);
    }
}