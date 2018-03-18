package com.gromoks.jmsdocumentdispatcher.service;

import com.gromoks.jmsdocumentdispatcher.entity.Document;

import javax.jms.Destination;

public interface JmsMessageService {
    String add(Destination destination, Document document);

    Document getById(Destination destination, String documentId, String targetConsumer);
}
