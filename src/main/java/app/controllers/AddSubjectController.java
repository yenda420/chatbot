package app.controllers;

import app.dao.SubjectManager;
import app.enums.UserRoleEnum;
import app.enums.ViewEnum;
import app.models.Subject;
import app.models.User;
import app.services.AlertService;
import app.services.DatabaseService;
import app.services.DynamicService;
import app.services.LoaderService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class AddSubjectController {
    private User currentUser;

    @FXML
    private Button addSubjectButton;

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
        addSubjectButton.setDefaultButton(true);
        subjectName.requestFocus();

        logoutButton.setOnMouseEntered(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-maroon.png"))));
        logoutButton.setOnMouseExited(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-white.png"))));
    }

    public void initializeUserData() {
        if (currentUser != null) {
            if (currentUser.getRole().equals(UserRoleEnum.ADMIN)) {
                DynamicService.setAdminNavigation(horizontalMenu, logoutButton, testsOverviewButton, getClass(), subjectName, currentUser, formContainer, ViewEnum.ADD_SUBJECT);
            }
        } else {
            System.err.println("[ERROR] - User is not set.");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void onAddSubject() throws SQLException {
        if (validateInputs()) {
            try {
                Subject subject = new Subject(subjectName.getText(), abbreviation.getText());

                if (SubjectManager.save(subject)) {
                    AlertService.showSuccessAlert("Předmět byl uloňen.");
                    LoaderService.load(ViewEnum.ADD_SUBJECT, getClass(), subjectName, currentUser);
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to save subject.");
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
            AlertService.showErrorAlert(subjectName, "Vyplňte, prosím, pole pro název předmětu.");
            return false;
        }

        if (isNumeric(name)) {
            AlertService.showErrorAlert(subjectName, "Název předmětu by nemělo být pouze číslo.");
            return false;
        }

        if (abbr.isEmpty() || abbr.isBlank()) {
            AlertService.showErrorAlert(abbreviation, "Vyplňte, prosím, pole pro abreviatura předmětu.");
            return false;
        }

        abbreviation.setText(abbreviation.getText().trim().toUpperCase());
        abbr = abbr.trim().toUpperCase();

        if (!abbr.matches("[A-Z]{3}")) {
            AlertService.showErrorAlert(abbreviation, "Zkratka musí obsahovat přesně tři písmena bez mezer, čísel nebo speciálních znaků.");
            return false;
        }

        if (DatabaseService.instanceInDatabase("subjects", "name", subjectName.getText())) {
            AlertService.showErrorAlert(subjectName, "Předmět s názvem '" + subjectName.getText() + "' již existuje.");
            return false;
        }

        if (DatabaseService.instanceInDatabase("subjects", "abbreviation", abbreviation.getText())) {
            AlertService.showErrorAlert(subjectName, "Předmět se zkratkou '" + abbreviation.getText() + "' již existuje.");
            return false;
        }

        return true;
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }
}
