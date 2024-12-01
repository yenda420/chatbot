package app.enums;

public enum DifficultyEnum {
    EASY("Lehká"), MEDIUM("Těžká"), HARD("Střední");

    private final String name;

    // Enum constructor
    DifficultyEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static DifficultyEnum fromString(String name) {
        for (DifficultyEnum difficulty : DifficultyEnum.values()) {
            if (difficulty.name.equalsIgnoreCase(name)) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("[ERROR] - No constant with name " + name + " found");
    }
}
