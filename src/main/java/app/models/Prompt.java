package app.models;

import java.io.File;
import java.util.ArrayList;

public class Prompt {
    private String message;
    private File attachedFile;
    private ArrayList<Topic> topics;
    private String tags;

    public Prompt(String message, File attachedFile, ArrayList<Topic> topics, String tags) {
        this.message = message;
        this.attachedFile = attachedFile;
        this.topics = topics;
        this.tags = tags;
    }

    public Prompt(File attachedFile, ArrayList<Topic> topics, String tags) {
        this.attachedFile = attachedFile;
        this.topics = topics;
        this.tags = tags;
    }

    public Prompt(String message, ArrayList<Topic> topics, String tags) {
        this.message = message;
        this.topics = topics;
        this.tags = tags;
    }

    public Prompt(ArrayList<Topic> topics, String tags) {
        this.topics = topics;
        this.tags = tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getMessage() {
        return message;
    }

    public File getAttachedFile() {
        return attachedFile;
    }

    public ArrayList<Topic> getTopics() {
        return topics;
    }

    public String getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "Prompt{" +
                "message='" + message + '\'' +
                ", attachedFile=" + attachedFile +
                ", topics=" + topics +
                ", tags='" + tags + '\'' +
                '}';
    }
}
