package com.gromoks.jmsdocumentdispatcher.controller;

import com.gromoks.jmsdocumentdispatcher.entity.Document;
import com.gromoks.jmsdocumentdispatcher.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/document", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DocumentController {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> add(@RequestBody Document document) {
        log.info("Sending request to add document");
        long startTime = System.currentTimeMillis();

        documentService.add(document);

        log.info("Document has been added. It tooks {} ms", System.currentTimeMillis() - startTime);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
