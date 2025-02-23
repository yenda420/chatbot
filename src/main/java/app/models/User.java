package app.models;

import app.dao.UserManager;
import app.enums.UserRoleEnum;

import javafx.collections.ObservableList;

public class User {
    private int id;
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

    public User(String email, String passwordHash, UserRoleEnum role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public User(String email) {
        this.email = email;
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

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(UserRoleEnum role) {
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean relate(ObservableList<String> subjects) {
        return UserManager.linkUserToSubjects(subjects, this);
    }

    public boolean unrelate(ObservableList<String> subjects) {
        return UserManager.unlinkUserFromSubjects(subjects, this);
    }
}
