    package app.controllers;

    import app.dao.SubjectManager;
    import app.dao.UserManager;
    import app.enums.UserRoleEnum;
    import app.enums.ViewEnum;
    import app.models.User;
    import app.services.DatabaseService;
    import app.services.LoaderService;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.fxml.FXML;
    import javafx.scene.control.*;

    import java.nio.charset.StandardCharsets;
    import java.sql.SQLException;
    import com.google.common.hash.Hashing;

    public class RegisterController {
        private static final double CELL_HEIGHT_SMALLER = 36.7;
        private static final double CELL_HEIGHT_BIGGER = 39.9;
        private static final int MAX_CELLS_TO_SHOW = 11;

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
        private Button registerButton;

        @FXML
        private void initialize() {
            registerButton.setDefaultButton(true);
            firstName.requestFocus();

            subjects.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            subjects.setItems(subjectList);

            int numberOfCells = subjectList.size();

            if (numberOfCells > MAX_CELLS_TO_SHOW) {
                subjects.setPrefHeight(MAX_CELLS_TO_SHOW * CELL_HEIGHT_SMALLER);
            } else if (numberOfCells < 5) {
                subjects.setPrefHeight(numberOfCells * CELL_HEIGHT_BIGGER);
            } else {
                subjects.setPrefHeight(numberOfCells * CELL_HEIGHT_SMALLER);
            }
        }

        public void handleLoginLink() {
            LoaderService.load(ViewEnum.LOGIN, getClass(), firstName, null);
        }

        @FXML
        private void handleRegister() throws SQLException {
            if (validateInputs()) {
                String passwordHash = Hashing.sha256()
                        .hashString(password.getText(), StandardCharsets.UTF_8)
                        .toString();

                User user = new User(firstName.getText(), lastName.getText(), email.getText(), passwordHash, UserRoleEnum.TEACHER);

                if (UserManager.save(user)) {
                    user.setId(UserManager.getId(user.getEmail()));

                    if (user.relate(subjects.getSelectionModel().getSelectedItems())) {
                        LoaderService.load(ViewEnum.MAIN, getClass(), firstName, user);
                    }
                }
            }
        }

        private boolean validateInputs() throws SQLException {
            // Using early return
            if (!emailIsValid(email.getText())) {
                showErrorAlert(email, "Zadejte, prosím, platnou emailovou adresu.");
                return false;
            }

            if (DatabaseService.instanceInDatabase("users", "email", email.getText())) {
                showErrorAlert(email, "Uživatel s touto emailovou adresou již existuje.");
                return false;
            }

            if (password.getText().isBlank() || password.getText().isEmpty()) {
                showErrorAlert(password, "Vyplňte, prosím, pole pro heslo.");
                return false;
            }

            if (!passwordIsValid(password.getText())) {
                showErrorAlert(password, "Heslo musí obsahovat alespoň 8 znáků, číslo, velké i malé písmeno a speciální znak.");
                return false;
            }

            if (!password.getText().equals(confirmPassword.getText())) {
                showErrorAlert(confirmPassword, "Hesla se neshodují.");
                return false;
            }

            if (subjects.getSelectionModel().getSelectedItems().isEmpty()) {
                showErrorAlert("Vyberte, prosím, alespoň jeden předmět.");
                return false;
            }

            return true;
        }

        private void showErrorAlert(Control control, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.showAndWait();
            control.requestFocus();
        }

        private void showErrorAlert(String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.showAndWait();
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