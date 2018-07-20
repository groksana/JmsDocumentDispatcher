package com.gromoks.jmsdocumentdispatcher.controller;

import com.gromoks.jmsdocumentdispatcher.entity.Document;
import com.gromoks.jmsdocumentdispatcher.service.DocumentDispatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/document", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DocumentController {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private DocumentDispatcherService documentDispatcherService;

    @Autowired
    public DocumentController(DocumentDispatcherService documentDispatcherService) {
        this.documentDispatcherService = documentDispatcherService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> add(@RequestBody Document document) {
        log.info("Sending request to add document");
        log.debug("Document {}", document);
        long startTime = System.currentTimeMillis();

        documentDispatcherService.add(document);

        log.info("Document has been added. It took {} ms", System.currentTimeMillis() - startTime);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getById(@PathVariable String id) {
        log.info("Sending request to get document by id = {}", id);
        long startTime = System.currentTimeMillis();

        Document document = documentDispatcherService.getById(id);

        log.info("Movies are received. It took {} ms", System.currentTimeMillis() - startTime);
        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getByKeyWords(@RequestBody List<String> keyWordList) {
        log.info("Sending request to get document by key words");
        long startTime = System.currentTimeMillis();

        List<Document> documents = documentDispatcherService.getByKeyWords(keyWordList);

        log.info("Documents have been found. It took {} ms", System.currentTimeMillis() - startTime);
        return new ResponseEntity<Object>(documents, HttpStatus.OK);
    }

}
