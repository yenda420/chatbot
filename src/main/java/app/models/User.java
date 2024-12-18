package app.models;

import app.enums.UserRoleEnum;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private UserRoleEnum role;

    public User(String firstName, String lastName, String email, String passwordHash, UserRoleEnum role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRoleEnum getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", role=" + role +
                '}';
    }
}
