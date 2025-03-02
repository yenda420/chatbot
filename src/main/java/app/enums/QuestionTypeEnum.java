package app.enums;

public enum QuestionTypeEnum {
    YES_NO("Ano / Ne"),
    MULTIPLE_CHOICE("Výběr z odpovědí"),
    OPEN_ENDED("Otevřená otázka");

    private final String name;

    // Enum constructor
    QuestionTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Returns the question type enum by name
    public static QuestionTypeEnum fromString(String name) {
        for (QuestionTypeEnum questionType : QuestionTypeEnum.values()) {
            if (questionType.name.equalsIgnoreCase(name)) {
                return questionType;
            }
        }
        throw new IllegalArgumentException("No constant with name " + name + " found");
    }
}
