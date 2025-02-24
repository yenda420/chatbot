    package app.controllers;

    import app.dao.SubjectManager;
    import app.dao.UserManager;
    import app.enums.UserRoleEnum;
    import app.enums.ViewEnum;
    import app.models.User;
    import app.services.AlertService;
    import app.services.DatabaseService;
    import app.services.DynamicService;
    import app.services.LoaderService;
    import com.google.common.hash.Hashing;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.fxml.FXML;
    import javafx.scene.control.*;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.layout.HBox;
    import javafx.scene.layout.VBox;
    import javafx.scene.text.Text;

    import java.nio.charset.StandardCharsets;
    import java.sql.SQLException;

    public class AddUserController {
        private User currentUser;

        private final ObservableList<String> subjectList =
                FXCollections.observableArrayList(SubjectManager.getSubjects());

        @FXML
        private TextField firstName;

        @FXML
        private TextField lastName;

        @FXML
        private TextField email;

        @FXML
        private PasswordField password;

        @FXML
        private PasswordField confirmPassword;

        @FXML
        private ListView<String> subjects;

        @FXML
        private CheckBox confirmationCheckbox;

        @FXML
        private Button saveUserButton;

        @FXML
        private ComboBox<String> role;

        @FXML
        private Text selectSubjectsHeading;

        @FXML
        private ImageView logoutIcon;

        @FXML
        private Button logoutButton;

        @FXML
        private HBox horizontalMenu;

        @FXML
        private Button testsOverviewButton;

        @FXML
        private VBox formContainer;

        @FXML
        private void initialize() {
            saveUserButton.setDefaultButton(true);
            firstName.requestFocus();
            subjects.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            logoutButton.setOnMouseEntered(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-maroon.png"))));
            logoutButton.setOnMouseExited(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-white.png"))));
            role.setItems(FXCollections.observableArrayList(UserManager.getUserRoles()));
        }

        public void initializeUserData() {
            if (currentUser.getRole().equals(UserRoleEnum.ADMIN)) {
                DynamicService.setAdminNavigation(horizontalMenu, logoutButton, testsOverviewButton, getClass(), firstName, currentUser, formContainer, ViewEnum.ADD_USER);
            }
        }

        @FXML
        private void onSaveUser() throws SQLException {
            if (validateInputs()) {
                String passwordHash = Hashing.sha256()
                        .hashString(password.getText(), StandardCharsets.UTF_8)
                        .toString();

                User user = new User(firstName.getText(), lastName.getText(), email.getText(), passwordHash, UserRoleEnum.fromString(role.getValue()));

                if (UserManager.save(user)) {
                    user.setId(UserManager.getId(user.getEmail()));

                    if (user.relate(subjects.getSelectionModel().getSelectedItems())) {
                        AlertService.showSuccessAlert("Uživatel byl uložen do databáze.");
                        LoaderService.load(ViewEnum.ADD_USER, getClass(), firstName, currentUser);
                    }
                }
            }
        }

        @FXML
        public void onChooseRole() {
            if (!role.getValue().isEmpty() && !role.getValue().isBlank() && role.getValue() != null) {
                if (role.getValue().equalsIgnoreCase(UserRoleEnum.TEACHER.getName())) {
                    selectSubjectsHeading.setText("Vyberte vyučované předměty * (Ctrl + Klik):");
                    subjects.setItems(subjectList);

                    selectSubjectsHeading.setVisible(true);
                    subjects.setVisible(true);

                    selectSubjectsHeading.setStyle("-fx-pref-height: auto; -fx-pref-width: auto;");
                    subjects.setStyle("-fx-pref-height: 120; -fx-pref-width: 350;");
                } else {
                    selectSubjectsHeading.setVisible(false);
                    subjects.setVisible(false);

                    selectSubjectsHeading.setStyle("-fx-pref-height: 0; -fx-pref-width: 0;");
                    subjects.setStyle("-fx-pref-height: 0; -fx-pref-width: 0;");
                }
            }
        }

        @FXML
        public void onGoToMain() {
            LoaderService.load(ViewEnum.MAIN, getClass(), firstName, currentUser);
        }

        @FXML
        public void onGoToTestsOverview() {
            LoaderService.load(ViewEnum.TESTS_OVERVIEW, getClass(), firstName, currentUser);
        }

        @FXML
        public void onGoToEditProfile() {
            LoaderService.load(ViewEnum.EDIT_PROFILE, getClass(), firstName, currentUser);
        }

        @FXML
        public void onGoToTopicsOverview() {
            LoaderService.load(ViewEnum.TOPICS_OVERVIEW, getClass(), firstName, currentUser);
        }

        @FXML
        public void onGoToTopics() {
            LoaderService.load(ViewEnum.ADD_TOPIC, getClass(), firstName, currentUser);
        }

        @FXML
        public void onLogout() {
            LoaderService.load(ViewEnum.LOGIN, getClass(), firstName, currentUser);
        }

        private boolean validateInputs() throws SQLException {
            // Using early return
            if (!emailIsValid(email.getText())) {
                AlertService.showErrorAlert(email, "Zadejte, prosím, platnou emailovou adresu.");
                return false;
            }

            if (DatabaseService.instanceInDatabase("users", "email", email.getText())) {
                AlertService.showErrorAlert(email, "Uživatel s touto emailovou adresou již existuje.");
                return false;
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

            if (role.getValue() == null || role.getValue().isEmpty() || role.getValue().isBlank()) {
                AlertService.showErrorAlert("Vyberte, prosím, roli.");
                return false;
            }

            if (subjects.getSelectionModel().getSelectedItems().isEmpty() && role.getValue().equalsIgnoreCase(UserRoleEnum.TEACHER.getName())) {
                AlertService.showErrorAlert("Vyberte, prosím, alespoň jeden předmět.");
                return false;
            }

            if (!confirmationCheckbox.isSelected()) {
                AlertService.showErrorAlert("Potvrďte, prosím, přečtení Manuálu aplikace.");
                return false;
            }

            return true;
        }

        public void setCurrentUser(User user) {
            this.currentUser = user;
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