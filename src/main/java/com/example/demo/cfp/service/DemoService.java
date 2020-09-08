package com.example.demo.cfp.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DemoService {
  public void processDocument(JsonNode document) {
    if (document == null) return;
    log.info(document.toPrettyString());
  }
}
