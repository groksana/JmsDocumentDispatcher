package com.gromoks.jmsdocumentdispatcher.service.impl;

import com.gromoks.jmsdocumentdispatcher.entity.Document;
import com.gromoks.jmsdocumentdispatcher.service.MessageService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.List;
import java.util.UUID;

import static com.gromoks.jmsdocumentdispatcher.util.JsonJacksonConverter.parseValue;
import static com.gromoks.jmsdocumentdispatcher.util.JsonJacksonConverter.toJson;

@Service
public class JmsMessageService implements MessageService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private JmsTemplate jmsTemplate;

    private JmsTemplate jmsTopicTemplate;

    private final Queue requestQueue;

    private final Queue responseQueue;

    private final Topic requestTopic;

    @Autowired
    public JmsMessageService(@Qualifier("jmsTemplate") JmsTemplate jmsTemplate,
                             @Qualifier("jmsTopicTemplate") JmsTemplate jmsTopicTemplate,
                             @Value("${jms.document.request.queue}") String requestQueueName,
                             @Value("${jms.document.response.queue}") String responseQueueName,
                             @Value("${jms.document.search.request.topic}") String requestTopicName) {
        this.jmsTemplate = jmsTemplate;
        this.jmsTopicTemplate = jmsTopicTemplate;

        requestQueue = new ActiveMQQueue(requestQueueName);
        responseQueue = new ActiveMQQueue(responseQueueName);
        requestTopic = new ActiveMQTopic(requestTopicName);
    }

    @Override
    public String add(Document document) {
        log.debug("Start to process document with id = {}", document.getId());
        long startTime = System.currentTimeMillis();

        String correlationID = UUID.randomUUID().toString();

        jmsTemplate.send(requestQueue, session -> {
            Message message = session.createTextMessage(toJson(document));
            message.setJMSReplyTo(responseQueue);
            message.setJMSCorrelationID(correlationID);
            message.setStringProperty("operation", "add.AnyDatabase");
            return message;
        });

        String filter = "JMSCorrelationID = '" + correlationID + "'";
        Message message = jmsTemplate.receiveSelected(responseQueue, filter);
        String databaseName;

        try {
            databaseName = message.getStringProperty("database");
        } catch (JMSException e) {
            log.error("Can't get JMS message with error: {}", e);
            throw new RuntimeException(e);
        }

        log.debug("Finish to process document. It took {} ms", System.currentTimeMillis() - startTime);
        return databaseName;
    }

    @Override
    public Document getById(String documentId, String targetConsumer) {
        log.debug("Start to process document with id = {}", documentId);
        long startTime = System.currentTimeMillis();

        String correlationID = UUID.randomUUID().toString();

        jmsTemplate.send(requestQueue, session -> {
            Message message = session.createTextMessage(documentId);
            message.setJMSReplyTo(responseQueue);
            message.setJMSCorrelationID(correlationID);
            message.setStringProperty("operation", "get." + targetConsumer);
            return message;
        });

        String filter = "JMSCorrelationID = '" + correlationID + "'";
        Message message = jmsTemplate.receiveSelected(responseQueue, filter);
        Document receivedDocument;

        try {
            String loadedMessage = ((TextMessage) message).getText();
            receivedDocument = parseValue(loadedMessage, Document.class);
        } catch (JMSException e) {
            log.error("Can't get JMS message with error: {}", e);
            throw new RuntimeException(e);
        }

        log.debug("Finish to process document. It took {} ms", System.currentTimeMillis() - startTime);
        return receivedDocument;
    }

    @Override
    public void publishKeyWords(List<String> keyWordList, String requestId) {
        log.debug("Start to publish key words for document search: {}", keyWordList);
        long startTime = System.currentTimeMillis();

        jmsTopicTemplate.send(requestTopic, session -> {
            Message message = session.createTextMessage(toJson(keyWordList));
            message.setStringProperty("requestId", requestId);
            return message;
        });

        log.debug("Finish to publish key words for document search. It took {} ms", System.currentTimeMillis() - startTime);
    }
}

