package app.enums;

public enum UserRoleEnum {
    ADMIN("Administrátor"), TEACHER("Učitel");
    private final String name;

    // Enum constructor
    UserRoleEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Returns the user role enum by name
    public static UserRoleEnum fromString(String name) {
        for (UserRoleEnum role : UserRoleEnum.values()) {
            if (role.name.equalsIgnoreCase(name)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No constant with name " + name + " found");
    }
}
