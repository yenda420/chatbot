package app.enums;

import app.models.Subject;

public enum SubjectEnum {
    CZECH("Český jazyk", "ČJL"),
    ENGLISH("Anglický jazyk", "ANJ"),
    PROGRAMMING("Programování", "PRG");

    private final Subject subject;

    // Enum constructor
    SubjectEnum(String name, String abbreviation) {
        this.subject = new Subject(name, abbreviation);
    }

    // Getter to return the Subject instance
    public Subject getSubject() {
        return subject;
    }
}