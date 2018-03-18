package com.gromoks.jmsdocumentdispatcher.service;

import com.gromoks.jmsdocumentdispatcher.entity.Document;

import javax.jms.Destination;

public interface JmsMessageService {
    String send(Destination destination, Document document);
}
