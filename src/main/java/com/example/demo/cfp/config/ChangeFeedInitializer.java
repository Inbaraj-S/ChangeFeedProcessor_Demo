package com.example.demo.cfp.config;

import com.azure.cosmos.ChangeFeedProcessor;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChangeFeedInitializer {

  @Autowired private ChangeFeedBuilder changeFeedBuilder;

  private ChangeFeedProcessor changeFeedProcessor;

  /** Setup change feed processor. */
  @PostConstruct
  public void setupAndStartChangeFeed() {
    try {
      log.info("^^^^^^^^^^^ Subscribed ChangeFeed starting.. ^^^^^^^^^^^^^^^^^^");
      changeFeedProcessor = changeFeedBuilder.buildChangeFeeProcessor();
      startChangeFeed();
      log.info("^^^^^^^^^^^ Subscribed to changeFeed is successful ^^^^^^^^^^^^^^^^^^");
    } catch (Exception e) {
      log.error("Error occurred while starting changeFeedProcessor.", e);
      throw e;
    }
  }

  private void startChangeFeed() {
    if (changeFeedProcessor != null) {
      changeFeedProcessor.start().block();
    } else {
      log.warn("changeFeedProcessor is null.. probably changeFeedProcessor has not been setup yet");
    }
  }

  /** Cleanup. */
  @PreDestroy
  public void stopChangeFeed() {

    log.info("Shutting down change feed.");

    if (changeFeedProcessor != null) {
      changeFeedProcessor.stop().block();
    } else {
      log.warn(
          "changeFeedProcessor is null.. probably changeFeedProcessor has not been started yet");
    }
  }
}