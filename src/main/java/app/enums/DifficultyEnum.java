package app.enums;

public enum DifficultyEnum {
    EASY("Lehká"), MEDIUM("Střední"), HARD("Těžká");

    private final String name;

    // Enum constructor
    DifficultyEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Returns the difficulty enum by name
    public static DifficultyEnum fromString(String name) {
        for (DifficultyEnum difficulty : DifficultyEnum.values()) {
            if (difficulty.name.equalsIgnoreCase(name)) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("No constant with name " + name + " found");
    }
}
