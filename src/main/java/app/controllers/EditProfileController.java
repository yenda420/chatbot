package app.controllers;

import app.dao.SubjectManager;

import app.dao.UserManager;
import app.enums.ViewEnum;

import app.models.User;

import app.services.AlertService;
import app.services.DatabaseService;
import app.services.LoaderService;

import com.google.common.hash.Hashing;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;

public class EditProfileController {
    private static final double CELL_HEIGHT_SMALLER = 36.7;
    private static final double CELL_HEIGHT_BIGGER = 39.9;
    private static final int MAX_CELLS_TO_SHOW = 11;

    private final ObservableList<String> subjectList =
            FXCollections.observableArrayList(SubjectManager.getSubjects());
    private User currentUser;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private TextField email;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private PasswordField password;

    @FXML
    private Button saveButton;

    @FXML
    private ListView<String> subjects;

    private void updateFields() throws SQLException {
        if (currentUser != null) {
            ArrayList<String> usersSubjects = SubjectManager.getSubjects(currentUser);
            int numberOfCells = subjectList.size();

            firstName.setText(currentUser.getFirstName() != null ? currentUser.getFirstName() : "");
            lastName.setText(currentUser.getLastName() != null ? currentUser.getLastName() : "");
            email.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");

            if (numberOfCells > MAX_CELLS_TO_SHOW) {
                subjects.setPrefHeight(MAX_CELLS_TO_SHOW * CELL_HEIGHT_SMALLER);
            } else if (numberOfCells < 5) {
                subjects.setPrefHeight(numberOfCells * CELL_HEIGHT_BIGGER);
            } else {
                subjects.setPrefHeight(numberOfCells * CELL_HEIGHT_SMALLER);
            }

            for (String subject : usersSubjects) {
                subjects.getSelectionModel().select(subject);
            }
        } else {
            System.err.println("[ERROR] - Current user is not null.");
        }
    }


    @FXML
    private void initialize() {
        saveButton.setDefaultButton(true);
        firstName.requestFocus();
        subjects.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        subjects.setItems(subjectList);
    }

    public void initializeUserData() throws SQLException {
        if (currentUser != null) {
            ArrayList<String> allSubjects = SubjectManager.getSubjects();
            ObservableList<String> subjectsObservable = FXCollections.observableArrayList(allSubjects);

            subjects.setItems(subjectsObservable);
            updateFields();
        } else {
            System.err.println("[ERROR] - User is not set.");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("[INFO] - Current user: " + user.getEmail());
    }

    @FXML
    public void onGoToMain() {
        LoaderService.load(ViewEnum.MAIN, getClass(), email, currentUser);
    }

    @FXML
    public void onGoToTopics() {
        LoaderService.load(ViewEnum.ADD_TOPIC, getClass(), email, currentUser);
    }

    @FXML
    public void onSave() throws SQLException {
        if (validateInputs()) {
            String oldEmail = currentUser.getEmail();
            String passwordHash = Hashing.sha256()
                    .hashString(password.getText(), StandardCharsets.UTF_8)
                    .toString();

            currentUser.setFirstName(firstName.getText());
            currentUser.setLastName(lastName.getText());
            currentUser.setEmail(email.getText());
            currentUser.setPasswordHash(passwordHash);

            if (UserManager.update(currentUser, oldEmail) &&
                currentUser.relate(subjects.getSelectionModel().getSelectedItems())) {
                AlertService.showSuccessAlert("Profil byl aktualizovan.");
            }
        }
    }

    private boolean validateInputs() throws SQLException {
        // Using early return
        if (!currentUser.getEmail().equals(email.getText())) {
            if (!emailIsValid(email.getText())) {
                AlertService.showErrorAlert(email, "Zadejte, prosím, platnou emailovou adresu.");
                return false;
            }

            if (DatabaseService.instanceInDatabase("users", "email", email.getText())) {
                AlertService.showErrorAlert(email, "Uživatel s touto emailovou adresou již existuje.");
                return false;
            }
        }

        if (password.getText().isBlank() || password.getText().isEmpty()) {
            AlertService.showErrorAlert(password, "Vyplňte, prosím, pole pro heslo.");
            return false;
        }

        if (!passwordIsValid(password.getText())) {
            AlertService.showErrorAlert(password, "Heslo musí obsahovat alespoň 8 znáků, číslo, velké i malé písmeno a speciální znak.");
            return false;
        }

        if (!password.getText().equals(confirmPassword.getText())) {
            AlertService.showErrorAlert(confirmPassword, "Hesla se neshodují.");
            return false;
        }

        if (subjects.getSelectionModel().getSelectedItems().isEmpty()) {
            AlertService.showErrorAlert("Vyberte, prosím, alespoň jeden předmět.");
            return false;
        }

        return true;
    }

    private boolean emailIsValid(String email) {
        // This regex validates the email format, ensuring it has:
        // a valid username, an '@' symbol, a domain name, and a top-level domain (2-4 letters).
        String emailPattern = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(emailPattern);
    }

    private boolean passwordIsValid(String password) {
        // This regex ensures the password is 8-20 characters long and contains:
        // at least one digit, one letter, and one special character from !@#$%^&*\.
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*\\\\/])[a-zA-Z0-9!@#$%^&*\\\\/]{8,20}$";
        return password.matches(passwordPattern);
    }
}
