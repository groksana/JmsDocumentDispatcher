package com.gromoks.jmsdocumentdispatcher.service.impl;

import com.gromoks.jmsdocumentdispatcher.entity.Document;
import com.gromoks.jmsdocumentdispatcher.service.DocumentService;
import com.gromoks.jmsdocumentdispatcher.service.DocumentDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    private DocumentDispatcher documentDispatcher;

    @Autowired
    public DocumentServiceImpl(DocumentDispatcher documentDispatcher) {
        this.documentDispatcher = documentDispatcher;
    }

    @Override
    public void add(Document document) {
        documentDispatcher.add(document);
    }

    @Override
    public Document getById(String documentId) {
        return documentDispatcher.getById(documentId);
    }

    @Override
    public List<Document> getByKeyWords(List<String> keyWordList) {
        return documentDispatcher.getByKeyWords(keyWordList);
    }
}
