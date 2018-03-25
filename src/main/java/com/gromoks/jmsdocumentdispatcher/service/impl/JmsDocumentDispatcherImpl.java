package com.gromoks.jmsdocumentdispatcher.service.impl;

import com.gromoks.jmsdocumentdispatcher.entity.Document;
import com.gromoks.jmsdocumentdispatcher.service.JmsDocumentDispatcher;
import com.gromoks.jmsdocumentdispatcher.service.JmsMessageService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import javax.jms.Topic;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class JmsDocumentDispatcherImpl implements JmsDocumentDispatcher {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, String> documentMap = new ConcurrentHashMap<>();

    private final Map<String, List<Document>> documentResponseMap = new ConcurrentHashMap<>();

    private final Map<String, Integer> requestProcessingMap = new ConcurrentHashMap<>();

    @Value("${document.queue}")
    private String queueName;

    @Value("${document.topic}")
    private String topicName;

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
        document.setId(documentId);
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

    @Override
    public List<Document> getByKeyWords(List<String> keyWordList) {
        log.debug("Start to search documents by key words: {}" + keyWordList);
        long startTime = System.currentTimeMillis();

        Topic topic = new ActiveMQTopic(topicName);
        String requestId = UUID.randomUUID().toString();
        jmsMessageService.publishKeyWords(topic, keyWordList, requestId);

        long consumerCount = documentMap.values().stream().distinct().count();
        Integer responseCount = requestProcessingMap.get(requestId) == null ? 0 : requestProcessingMap.get(requestId);
        while (responseCount < consumerCount || (System.currentTimeMillis() - startTime) < 5000) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("The thread has been interrupted: {}", e);
                throw new RuntimeException(e);
            }
            responseCount = requestProcessingMap.get(requestId) == null ? 0 : requestProcessingMap.get(requestId);
        }

        List<Document> foundDocument = documentResponseMap.get(requestId);

        log.debug("Finish to search documents by key words");
        return foundDocument;
    }

    @Override
    public void processSearchResponse(List<Document> documentList, String requestId) {
        log.debug("Start to process response by requestId: {}" + requestId);

        documentResponseMap.merge(requestId, documentList, (v1, v2) -> Stream.of(v1, v2)
                .flatMap(x -> x.stream())
                .collect(Collectors.toList()));
        requestProcessingMap.merge(requestId, 1, (v1, v2) -> v1 + v2);

        log.debug("Finish to process response by requestId: {}" + requestId);
    }
}
