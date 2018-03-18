package com.gromoks.jmsdocumentdispatcher.entity;

import java.io.Serializable;
import java.util.List;

public class Document {
    private String documentId;
    private List<String> context;

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public List<String> getContext() {
        return context;
    }

    public void setContext(List<String> context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "Document{" +
                "documentId=" + documentId +
                ", context=" + context +
                '}';
    }
}
