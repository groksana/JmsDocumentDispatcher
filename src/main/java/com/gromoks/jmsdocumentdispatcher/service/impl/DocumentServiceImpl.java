package com.gromoks.jmsdocumentdispatcher.service.impl;

import com.gromoks.jmsdocumentdispatcher.entity.Document;
import com.gromoks.jmsdocumentdispatcher.service.DocumentService;
import com.gromoks.jmsdocumentdispatcher.service.JmsDocumentDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private JmsDocumentDispatcher jmsDocumentDispatcher;

    @Autowired
    public DocumentServiceImpl(JmsDocumentDispatcher jmsDocumentDispatcher) {
        this.jmsDocumentDispatcher = jmsDocumentDispatcher;
    }

    @Override
    public void add(Document document) {
        jmsDocumentDispatcher.add(document);
    }

    @Override
    public Document getById(String documentId) {
        return jmsDocumentDispatcher.getById(documentId);
    }

    @Override
    public List<Document> getByKeyWords(List<String> keyWordList) {
        return jmsDocumentDispatcher.getByKeyWords(keyWordList);
    }
}
