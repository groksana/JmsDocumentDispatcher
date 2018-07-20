package com.gromoks.jmsdocumentdispatcher.service;

import com.gromoks.jmsdocumentdispatcher.entity.Document;

import javax.jms.Destination;
import java.util.List;

public interface MessageService {
    String add(Document document);

    Document getById(String documentId, String targetConsumer);

    void publishKeyWords(List<String> keyWordList, String requestId);
}
