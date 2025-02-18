package app.enums;

public enum ViewEnum {
    MAIN("main"),
    LOGIN("login"),
    REGISTER("register"),
    ADD_TOPIC("add-topic"),
    EDIT_TOPIC("edit-topic"),
    TOPICS_OVERVIEW("topics-overview"),

    EDIT_PROFILE("edit-profile");

    private final String name;

    // Enum constructor
    ViewEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
