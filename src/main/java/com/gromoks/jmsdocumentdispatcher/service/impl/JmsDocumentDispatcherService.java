package com.gromoks.jmsdocumentdispatcher.service.impl;

import com.gromoks.jmsdocumentdispatcher.entity.Document;
import com.gromoks.jmsdocumentdispatcher.service.DocumentDispatcherService;
import com.gromoks.jmsdocumentdispatcher.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class JmsDocumentDispatcherService implements DocumentDispatcherService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, String> documentMap = new ConcurrentHashMap<>();

    private final Map<String, List<Document>> documentResponseMap = new ConcurrentHashMap<>();

    private final Map<String, Integer> requestProcessingMap = new ConcurrentHashMap<>();

    @Value("${jms.document.search.request.topic}")
    private String topicName;

    private MessageService messageService;

    @Autowired
    public JmsDocumentDispatcherService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void add(Document document) {
        log.debug("Start to add document by dispatcher");

        String documentId = UUID.randomUUID().toString();
        document.setId(documentId);
        String databaseName = messageService.add(document);

        log.debug("Get destination Database name = {} for processed document", databaseName);

        documentMap.put(documentId, databaseName);

        log.debug("Finish to add document by dispatcher");
    }

    @Override
    public Document getById(String documentId) {
        log.debug("Start to get document by id = {}" + documentId);

        String targetDatabaseName = documentMap.get(documentId);
        Document receivedDocument = messageService.getById(documentId, targetDatabaseName);

        log.debug("Finish to get document by id");
        return receivedDocument;
    }

    @Override
    public List<Document> getByKeyWords(List<String> keyWordList) {
        log.debug("Start to search documents by key words: {}" + keyWordList);
        long startTime = System.currentTimeMillis();

        String requestId = UUID.randomUUID().toString();
        messageService.publishKeyWords(keyWordList, requestId);

        long consumerCount = documentMap.values().stream().distinct().count();
        Integer responseCount = requestProcessingMap.get(requestId) == null ? 0 : requestProcessingMap.get(requestId);
        while (responseCount < consumerCount || (System.currentTimeMillis() - startTime) < 5000) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("The thread has been interrupted: {}", e);
                throw new RuntimeException("The thread has been interrupted:", e);
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
