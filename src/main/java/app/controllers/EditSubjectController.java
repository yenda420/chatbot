package app.controllers;

import app.dao.SubjectManager;
import app.enums.UserRoleEnum;
import app.enums.ViewEnum;
import app.models.Subject;
import app.models.User;
import app.services.*;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class EditSubjectController {
    private User currentUser;

    private Subject subjectToEdit;

    @FXML
    private Button updateSubjectButton;

    @FXML
    private TextField subjectName;

    @FXML
    private TextField abbreviation;

    @FXML
    private Button logoutButton;

    @FXML
    private VBox formContainer;

    @FXML
    private Button testsOverviewButton;

    @FXML
    private ImageView logoutIcon;

    @FXML
    private HBox horizontalMenu;

    @FXML
    private void initialize() {
        updateSubjectButton.setDefaultButton(true);
        subjectName.requestFocus();

        logoutButton.setOnMouseEntered(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-maroon.png"))));
        logoutButton.setOnMouseExited(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-white.png"))));
    }

    public void initializeUserData() {
        if (currentUser != null) {
            if (currentUser.getRole().equals(UserRoleEnum.ADMIN)) {
                DynamicService.setAdminNavigation(horizontalMenu, logoutButton, testsOverviewButton, getClass(), subjectName, currentUser, formContainer, ViewEnum.EDIT_SUBJECT);
            }
        } else {
            LogService.logError("User is not set.");
        }

        if (subjectToEdit != null) {
            subjectName.setText(subjectToEdit.getName());
            abbreviation.setText(subjectToEdit.getAbbreviation());
        } else {
            LogService.logError("Subject to edit is not set.");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setSubjectToEdit(Subject subjectToEdit) {
        this.subjectToEdit = subjectToEdit;
    }

    @FXML
    public void onUpdateSubject() throws SQLException {
        if (validateInputs()) {
            try {
                Subject newSubject = new Subject(subjectName.getText(), abbreviation.getText());

                if (SubjectManager.update(subjectToEdit, newSubject)) {
                    AlertService.showSuccessAlert("Předmět byl aktualizován.");
                    LoaderService.load(ViewEnum.SUBJECTS_OVERVIEW, getClass(), subjectName, currentUser);
                }
            } catch (SQLException e) {
                LogService.logError("Failed to save subject.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onGoToMain() {
        LoaderService.load(ViewEnum.MAIN, getClass(), subjectName, currentUser);
    }

    @FXML
    public void onGoToTestsOverview() {
        LoaderService.load(ViewEnum.TESTS_OVERVIEW, getClass(), subjectName, currentUser);
    }

    @FXML
    public void onGoToEditProfile() {
        LoaderService.load(ViewEnum.EDIT_PROFILE, getClass(), subjectName, currentUser);
    }

    @FXML
    public void onGoToTopicsOverview() {
        LoaderService.load(ViewEnum.TOPICS_OVERVIEW, getClass(), subjectName, currentUser);
    }

    @FXML
    public void onLogout() {
        LoaderService.load(ViewEnum.LOGIN, getClass(), subjectName, currentUser);
    }

    @FXML
    public void onGoToTopics() {
        LoaderService.load(ViewEnum.ADD_TOPIC, getClass(), subjectName, currentUser);
    }

    private boolean validateInputs() throws SQLException {
        String name = subjectName.getText();
        String abbr = abbreviation.getText();

        if (name.isEmpty() || name.isBlank()) {
            AlertService.showErrorAlert(subjectName, "Vyplňte, prosím, pole pro předmětu.");
            return false;
        }

        if (isNumeric(name) || isNumeric(abbr)) {
            AlertService.showErrorAlert(subjectName, "Název předmětu ani zkratka by neměly být pouze číslo.");
            return false;
        }

        if (!subjectToEdit.getName().equals(name)) {
            if (DatabaseService.instanceInDatabase("subjects", "name", name)) {
                AlertService.showErrorAlert(subjectName, "Předmět s tímto názvem již existuje.");
                return false;
            }
        }

        if (!subjectToEdit.getAbbreviation().equals(abbr)) {
            if (DatabaseService.instanceInDatabase("subjects", "abbreviation", abbr)) {
                AlertService.showErrorAlert(abbreviation, "Předmět s touto zkratkou již existuje.");
                return false;
            }
        }

        if (abbr.length() != 3) {
            AlertService.showErrorAlert(abbreviation, "Zkratka musí obsahovat přesně tři písmena bez mezer, čísel nebo speciálních znaků.");
            return false;
        }

        abbreviation.setText(abbreviation.getText().trim().toUpperCase());
        return true;
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }
}
