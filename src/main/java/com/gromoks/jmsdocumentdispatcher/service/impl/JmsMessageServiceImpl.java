package com.gromoks.jmsdocumentdispatcher.service.impl;

import com.gromoks.jmsdocumentdispatcher.entity.Document;
import com.gromoks.jmsdocumentdispatcher.service.JmsMessageService;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.List;
import java.util.UUID;

import static com.gromoks.jmsdocumentdispatcher.util.JsonJacksonConverter.parseValue;
import static com.gromoks.jmsdocumentdispatcher.util.JsonJacksonConverter.toJson;

@Service
public class JmsMessageServiceImpl implements JmsMessageService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("jmsTemplate")
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("jmsTopicTemplate")
    private JmsTemplate jmsTopicTemplate;

    @Value("${document.queue.response}")
    private String responseQueueName;

    @Override
    public String add(Destination destination, Document document) {
        log.debug("Start to process document with id = {}", document.getDocumentId());
        long startTime = System.currentTimeMillis();

        String correlationID = UUID.randomUUID().toString();

        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message message = session.createTextMessage(toJson(document));
                message.setJMSReplyTo(new ActiveMQQueue(responseQueueName));
                message.setJMSCorrelationID(correlationID);
                message.setStringProperty("operation", "add.AnyDatabase");
                return message;
            }
        });

        Queue responseQueue = new ActiveMQQueue(responseQueueName);

        String filter = "JMSCorrelationID = '" + correlationID + "'";
        Message message = jmsTemplate.receiveSelected(responseQueue, filter);
        String databaseName = null;
        if (message instanceof TextMessage) {
            try {
                databaseName = message.getStringProperty("database");
            } catch (JMSException e) {
                log.error("Can't get JMS message with error: {}", e);
                throw new RuntimeException(e);
            }
        }

        log.debug("Finish to process document. It took {} ms", System.currentTimeMillis() - startTime);
        return databaseName;
    }

    @Override
    public Document getById(Destination destination, String documentId, String targetConsumer) {
        log.debug("Start to process document with id = {}", documentId);
        long startTime = System.currentTimeMillis();

        String correlationID = UUID.randomUUID().toString();

        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message message = session.createTextMessage(documentId);
                message.setJMSReplyTo(new ActiveMQQueue(responseQueueName));
                message.setJMSCorrelationID(correlationID);
                message.setStringProperty("operation", "get." + targetConsumer);
                return message;
            }
        });

        Queue responseQueue = new ActiveMQQueue(responseQueueName);

        String filter = "JMSCorrelationID = '" + correlationID + "'";
        Message message = jmsTemplate.receiveSelected(responseQueue, filter);
        Document receivedDocument = null;
        if (message instanceof TextMessage) {
            try {
                String loadedMessage = ((TextMessage) message).getText();
                receivedDocument = parseValue(loadedMessage, Document.class);
            } catch (JMSException e) {
                log.error("Can't get JMS message with error: {}", e);
                throw new RuntimeException(e);
            }
        }

        log.debug("Finish to process document. It took {} ms", System.currentTimeMillis() - startTime);
        return receivedDocument;
    }

    @Override
    public void publishKeyWords(Destination destination, List<String> keyWordList, String requestId) {
        log.debug("Start to publish key words for document search: {}", keyWordList);
        long startTime = System.currentTimeMillis();

        jmsTopicTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message message = session.createTextMessage(toJson(keyWordList));
                message.setStringProperty("requestId", requestId);
                return message;
            }
        });

        log.debug("Finish to publish key words for document search. It took {} ms", System.currentTimeMillis() - startTime);
    }
}

