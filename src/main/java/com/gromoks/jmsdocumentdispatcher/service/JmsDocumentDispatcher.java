package com.gromoks.jmsdocumentdispatcher.service;

import com.gromoks.jmsdocumentdispatcher.entity.Document;

public interface JmsDocumentDispatcher {
    void add(Document document);
}
