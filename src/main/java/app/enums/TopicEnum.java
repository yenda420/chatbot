package app.enums;

import app.models.Topic;
import app.enums.SubjectEnum;

public enum TopicEnum {
    // Topics for Český jazyk
    ANCIENT_LITERATURE("Antická literatura", SubjectEnum.CZECH),
    INTERWAR_LITERATURE("Meziválečná literatura", SubjectEnum.CZECH),
    REALISM("Realismus", SubjectEnum.CZECH),
    ROMANTISM("Romantismus", SubjectEnum.CZECH),

    // Topics for Anglický jazyk
    HISTORY_OF_CZECH_REPUBLIC("History of the Czech Republic", SubjectEnum.ENGLISH),
    PRAGUE("Prague", SubjectEnum.ENGLISH),
    UK("The United Kingdom of Great Britain and Northern Ireland", SubjectEnum.ENGLISH),
    USA("The USA", SubjectEnum.ENGLISH),

    // Topics for Programování
    C_POINTERS("Jazyk C - Pointery", SubjectEnum.PROGRAMMING),
    C_DYNAMIC_ARRAYS("Jazyk C - Dynamické pole", SubjectEnum.PROGRAMMING),
    JAVA_ARRAYLIST("Java - ArrayList", SubjectEnum.PROGRAMMING),
    JAVA_FILES("Java - Soubory", SubjectEnum.PROGRAMMING);

    private final Topic topic;

    // Enum constructor
    TopicEnum(String name, SubjectEnum subjectEnum) {
        this.topic = new Topic(name, subjectEnum.getSubject());
    }

    // Getter to return the Topic instance
    public Topic getTopic() {
        return topic;
    }
}