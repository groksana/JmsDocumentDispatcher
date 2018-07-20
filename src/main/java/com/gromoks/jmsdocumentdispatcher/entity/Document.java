package com.gromoks.jmsdocumentdispatcher.entity;

import java.util.List;

public class Document {
    private String id;
    private List<String> content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", content=" + content +
                '}';
    }
}
