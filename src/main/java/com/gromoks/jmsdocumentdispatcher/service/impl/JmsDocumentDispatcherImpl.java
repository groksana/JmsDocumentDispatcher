package com.gromoks.jmsdocumentdispatcher.service.impl;

import com.gromoks.jmsdocumentdispatcher.entity.Document;
import com.gromoks.jmsdocumentdispatcher.service.JmsDocumentDispatcher;
import com.gromoks.jmsdocumentdispatcher.service.JmsMessageService;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JmsDocumentDispatcherImpl implements JmsDocumentDispatcher {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, String> documentMap = new ConcurrentHashMap<>();

    @Value("${document.queue}")
    private String queueName;

    private JmsMessageService jmsMessageService;

    @Autowired
    public JmsDocumentDispatcherImpl(JmsMessageService jmsMessageService) {
        this.jmsMessageService = jmsMessageService;
    }

    @Override
    public void add(Document document) {
        log.debug("Start to add document by dispatcher");

        Queue requestQueue = new ActiveMQQueue(queueName);

        String documentId = UUID.randomUUID().toString();
        document.setDocumentId(documentId);
        String databaseName = jmsMessageService.add(requestQueue, document);

        log.debug("Get destination Database name = {} for processed document", databaseName);

        documentMap.put(documentId, databaseName);

        log.debug("Finish to add document by dispatcher");
    }

    @Override
    public Document getById(String documentId) {
        log.debug("Start to get document by id = {}" + documentId);

        Queue requestQueue = new ActiveMQQueue(queueName);
        String targetDatabaseName = documentMap.get(documentId);
        Document receivedDocument = jmsMessageService.getById(requestQueue, documentId, targetDatabaseName);

        log.debug("Finish to get document by id");
        return receivedDocument;
    }
}
