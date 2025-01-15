package app.models;

import java.io.File;
import java.util.ArrayList;

public class Prompt {
    private String message;
    private File attachedFile;
    private ArrayList<Topic> topics;

    public Prompt(String message, File attachedFile, ArrayList<Topic> topics) {
        this.message = message;
        this.attachedFile = attachedFile;
        this.topics = topics;
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

    @Override
    public String toString() {
        return "Prompt{" +
                "message='" + message + '\'' +
                ", attachedFile=" + attachedFile +
                ", topics=" + topics +
                '}';
    }
}
