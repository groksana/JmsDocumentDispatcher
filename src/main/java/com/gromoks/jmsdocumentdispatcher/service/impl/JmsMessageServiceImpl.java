package com.gromoks.jmsdocumentdispatcher.service.impl;

import com.gromoks.jmsdocumentdispatcher.entity.Document;
import com.gromoks.jmsdocumentdispatcher.service.JmsMessageService;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.UUID;

import static com.gromoks.jmsdocumentdispatcher.util.JsonJacksonConverter.parseValue;
import static com.gromoks.jmsdocumentdispatcher.util.JsonJacksonConverter.toJson;

@Service
public class JmsMessageServiceImpl implements JmsMessageService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private JmsTemplate jmsTemplate;

    @Value("${document.queue.response}")
    private String responseQueueName;

    @Autowired
    public JmsMessageServiceImpl(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public String send(Destination destination, Document document) {
        log.debug("Start to process document with id = {}", document.getDocumentId());
        long startTime = System.currentTimeMillis();

        String correlationID = UUID.randomUUID().toString();

        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message message = session.createTextMessage(toJson(document));
                message.setJMSReplyTo(new ActiveMQQueue(responseQueueName));
                message.setJMSCorrelationID(correlationID);
                message.setStringProperty("add", "anyDatabase");
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
                String loadedMessage = ((TextMessage) message).getText();
                Document receivedDocument = parseValue(loadedMessage, Document.class);
            } catch (JMSException e) {
                log.error("Can't get JMS message with error: {}", e);
                throw new RuntimeException(e);
            }
        }

        log.debug("Finish to process document. It took {} ms", System.currentTimeMillis() - startTime);
        return databaseName;
    }
}

