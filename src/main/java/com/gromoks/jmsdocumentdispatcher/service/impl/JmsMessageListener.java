package com.gromoks.jmsdocumentdispatcher.service.impl;

import com.gromoks.jmsdocumentdispatcher.entity.Document;
import com.gromoks.jmsdocumentdispatcher.service.DocumentDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

import static com.gromoks.jmsdocumentdispatcher.util.JsonJacksonConverter.*;

@Service
public class JmsMessageListener implements MessageListener {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private DocumentDispatcher documentDispatcher;

    @Autowired
    public JmsMessageListener(DocumentDispatcher documentDispatcher) {
        this.documentDispatcher = documentDispatcher;
    }

    @Override
    @JmsListener(destination = "${jms.document.search.response.queue}")
    public void onMessage(Message message) {
        try {
            log.debug("!!!Start to process message with id = {}", message.getJMSMessageID());

            List<Document> foundDocumentList = null;
            String requestId = message.getStringProperty("requestId");

            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String documentList = textMessage.getText();
                foundDocumentList = parseValue(documentList, List.class);
            }

            documentDispatcher.processSearchResponse(foundDocumentList, requestId);
        } catch (JMSException e) {
            log.error("Can't get JMS message with error: {}", e);
            throw new RuntimeException(e);
        }
    }
}
