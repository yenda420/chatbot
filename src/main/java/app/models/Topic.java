package app.models;

public class Topic {
    private String name;
    private String description;
    private Subject subject;

    public Topic(String name, Subject subject) {
        this.name = name;
        this.subject = subject;
    }

    public Topic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Subject getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", subject=" + subject +
                '}';
    }
}
