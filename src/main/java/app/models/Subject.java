package app.models;

public class Subject {
    private String name;
    private String shortage;
    private String description;

    public Subject(String name, String shortage, String description) {
        this.name = name;
        this.shortage = shortage;
        this.description = description;
    }

    public Subject(String name, String shortage) {
        this.name = name;
        this.shortage = shortage;
    }

    public String getName() {
        return name;
    }

    public String getShortage() {
        return shortage;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "name='" + name + '\'' +
                ", shortage='" + shortage + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
