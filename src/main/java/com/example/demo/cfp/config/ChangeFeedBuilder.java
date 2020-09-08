package com.example.demo.cfp.config;

import com.azure.cosmos.*;
import com.azure.cosmos.implementation.guava25.collect.Lists;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.example.demo.cfp.service.DemoService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class ChangeFeedBuilder {

  @Autowired private DemoService demoService;

  private String endpointUri;
  private String primaryKey;
  private String hostName;
  private String dbName;
  private String feedContainerId;
  private String leaseContainerId;

  /** Initialize feeder. */
  @PostConstruct
  public void init() {
    this.endpointUri = "";
    this.primaryKey = "";
    this.hostName = "host-1";
    this.dbName = "sourceDB";
    this.feedContainerId = "sourceContainer";
    this.leaseContainerId = "leaseContainer";
  }

  ChangeFeedProcessor buildChangeFeeProcessor() {

    CosmosAsyncClient client =
        new CosmosClientBuilder()
            .endpoint(endpointUri)
            .key(primaryKey)
            .preferredRegions(Lists.newArrayList("South Central US"))
            .consistencyLevel(ConsistencyLevel.SESSION)
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();

    CosmosAsyncContainer feedContainer = client.getDatabase(dbName).getContainer(feedContainerId);
    CosmosAsyncContainer leaseContainer = client.getDatabase(dbName).getContainer(leaseContainerId);
    if (feedContainer == null || leaseContainer == null) {
      throw new IllegalArgumentException(
          "Application could not start. feedContainer and/or leaseContainer is null.");
    }

    ChangeFeedProcessor changeFeedProcessor = getChangeFeedProcessor(feedContainer, leaseContainer);

    log.info("Created feed processor. Process in loop...");

    changeFeedProcessor.getEstimatedLag();
    return changeFeedProcessor;
  }

  private ChangeFeedProcessor getChangeFeedProcessor(
      CosmosAsyncContainer feedContainer, CosmosAsyncContainer leaseContainer) {

    log.info("Inside CFP processing...");
    ChangeFeedProcessorOptions cfOptions = new ChangeFeedProcessorOptions();
    cfOptions.setFeedPollDelay(Duration.ofMillis(100));

    return new ChangeFeedProcessorBuilder()
        .options(cfOptions)
        .hostName(hostName)
        .feedContainer(feedContainer)
        .leaseContainer(leaseContainer)
        .handleChanges(
            (List<JsonNode> docs) -> {
              for (JsonNode document : docs) {
                handleRecievedFeed(document);
              }
            })
        .buildChangeFeedProcessor();
  }

  private void handleRecievedFeed(JsonNode document) {
    log.info("ID----> " + document.get("id"));
    demoService.processDocument(document);
  }
}
