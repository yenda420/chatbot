package app.models;

import app.dao.TagManager;

import java.sql.SQLException;

public class Tag {
    private String tag;

    public Tag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "tag='" + tag + '\'' +
                '}';
    }

    public boolean relate(int testId) throws SQLException {
        return TagManager.linkTagToTest(this, testId);
    }
}
