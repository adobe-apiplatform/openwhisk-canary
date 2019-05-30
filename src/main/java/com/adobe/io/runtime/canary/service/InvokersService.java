/*
Copyright 2019 Adobe. All rights reserved.
This file is licensed to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
 */

package com.adobe.io.runtime.canary.service;

import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InvokersService {

  private static Logger LOG = LoggerFactory.getLogger(InvokersService.class);

  @Value("${invokers.controller.url}")
  private String controllerUrl;

  @Value("${invokers.endpoint}")
  private String invokersEndpoint;

  @Value("${invokers.threshold.percentage}")
  private Integer threshold;

  @Value("${invokers.maxfailures}")
  private Integer maxFailures;

  @Autowired private HealthService healthService;

  private boolean serviceHealthy = true;

  @Autowired private RestTemplate restTemplate;

  @Scheduled(cron = "${invokers.interval.check}")
  private void checkEndpoints() {

    RequestResult result = getInvokersStatus();
    boolean validThreshold =
        validateThreshold(result.getInvokersTotal(), result.getInvokersUp(), this.threshold);

    setHealthy(healthService.isServiceHealthy(validThreshold, maxFailures));
  }

  /**
   * Call "/invokers" endpoint
   *
   * @return RequestResult (total and up invokers)
   */
  protected RequestResult getInvokersStatus() {
    LinkedHashMap<String, String> response = new LinkedHashMap<String, String>();
    try {

      String url = controllerUrl + invokersEndpoint;
      response = restTemplate.getForObject(url, LinkedHashMap.class);

      response.forEach(
          (key, value) -> {
            LOG.debug(key + " -> " + value);
          });

    } catch (Exception ex) {
      LOG.error("Requst to invokers endpoint failed: " + ex.getLocalizedMessage());
    }

    int upOccurrences =
        (int) response.entrySet().stream().filter(p -> p.getValue().equals("up")).count();

    return new RequestResult(response.size(), upOccurrences);
  }

  // Validate response against the configured threshold
  protected boolean validateThreshold(int totalNodes, long upNodes, int threshold) {
    int percent = (int) ((upNodes * 100.0f) / totalNodes);

    if (percent > threshold) {
      return true;
    }

    return false;
  }

  private void setHealthy(boolean serviceHealthy) {
    this.serviceHealthy = serviceHealthy;
  }

  public boolean isHealthy() {
    return this.serviceHealthy;
  }
}

class RequestResult {
  private Integer invokersTotal;
  private Integer invokersUp;

  public RequestResult(Integer invokersTotal, Integer invokersUp) {
    this.invokersTotal = invokersTotal;
    this.invokersUp = invokersUp;
  }

  protected Integer getInvokersTotal() {
    return this.invokersTotal;
  }

  protected Integer getInvokersUp() {
    return this.invokersUp;
  }
}
