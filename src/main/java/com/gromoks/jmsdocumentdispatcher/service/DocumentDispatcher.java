package com.gromoks.jmsdocumentdispatcher.service;

import com.gromoks.jmsdocumentdispatcher.entity.Document;

import java.util.List;

public interface DocumentDispatcher {
    void add(Document document);

    Document getById(String documentId);

    List<Document> getByKeyWords(List<String> keyWordList);

    void processSearchResponse(List<Document> documentList, String requestId);
}
