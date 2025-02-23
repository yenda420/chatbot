package app.enums;

public enum ViewEnum {
    MAIN("main"),
    LOGIN("login"),
    REGISTER("register"),
    ADD_TOPIC("add-topic"),
    ADD_USER("add-user"),
    EDIT_TOPIC("edit-topic"),
    EDIT_PROFILE("edit-profile"),
    TOPICS_OVERVIEW("topics-overview"),
    TESTS_OVERVIEW("tests-overview"),
    USERS_OVERVIEW("users-overview");

    private final String name;

    // Enum constructor
    ViewEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
