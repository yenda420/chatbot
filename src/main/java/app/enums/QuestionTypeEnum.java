package app.enums;

public enum QuestionTypeEnum {
    YES_NO("Ano / Ne"),
    SELECT("Výběr z odpověí"),
    QUESTION("Otevřená otázka");

    private final String name;

    // Enum constructor
    QuestionTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static QuestionTypeEnum fromString(String name) {
        for (QuestionTypeEnum questionType : QuestionTypeEnum.values()) {
            if (questionType.name.equalsIgnoreCase(name)) {
                return questionType;
            }
        }
        throw new IllegalArgumentException("[ERROR] - No constant with name " + name + " found");
    }
}
