package app.enums;

public enum ViewEnum {
    MAIN("main"), LOGIN("login"), REGISTER("register");
    private final String name;

    // Enum constructor
    ViewEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Returns the view enum by name
    public static ViewEnum fromString(String name) {
        for (ViewEnum view : ViewEnum.values()) {
            if (view.name.equalsIgnoreCase(name)) {
                return view;
            }
        }
        throw new IllegalArgumentException("[ERROR] - No constant with name " + name + " found");
    }
}
