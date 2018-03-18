package com.gromoks.jmsdocumentdispatcher.service;

import com.gromoks.jmsdocumentdispatcher.entity.Document;

import javax.jms.Destination;
import java.util.List;

public interface JmsMessageService {
    String add(Destination destination, Document document);

    Document getById(Destination destination, String documentId, String targetConsumer);

    void publishKeyWords(Destination destination, List<String> keyWordList, String requestId);
}
