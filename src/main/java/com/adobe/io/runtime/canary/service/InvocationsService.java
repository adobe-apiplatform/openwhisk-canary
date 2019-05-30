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

import com.adobe.io.runtime.canary.model.Action;
import com.adobe.io.runtime.canary.model.Exec;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

@Service
public class InvocationsService {

  private static Logger LOG = LoggerFactory.getLogger(InvocationsService.class);

  @Autowired private RestTemplate restTemplate;

  @Value("${whisk.namespace}")
  private String namespace;

  @Value("${whisk.auth}")
  private String auth;

  @Value("${whisk.action.name}")
  private String actionName;

  @Value("${whisk.action.kind}")
  private String actionKind;

  @Value("${whisk.action.code}")
  private String actionCode;

  @Value("${invocations.controller.url}")
  private String controllerUrl;

  @Value("${whisk.action.endpoint}")
  private String actionEndpoint;

  protected HttpHeaders headers;
  protected String url;

  @Value("${invocations.maxfailures}")
  private Integer maxFailures;

  @Autowired private HealthService healthService;

  private boolean serviceHealthy = true;

  @Autowired MeterRegistry registry;

  @PostConstruct
  public void init() {

    BasicCredentials credentials = new BasicCredentials(auth);
    headers = new HttpHeaders();
    headers.setBasicAuth(credentials.getUsername(), credentials.getPassword());
    headers.setContentType(MediaType.APPLICATION_JSON);

    url = controllerUrl + actionEndpoint;

    createAction();
  }

  @Scheduled(cron = "${invocations.interval.check}")
  private void checkAction() {

    HttpStatus statusCode = getInvocationStatusCode();
    boolean healthy = healthService.isServiceHealthy(isStatusOk(statusCode), maxFailures);
    setHealthy(healthy);
  }

  // Only 200 should be considered healthy responses
  protected boolean isStatusOk(HttpStatus status) {
    if (status.value() < HttpStatus.INTERNAL_SERVER_ERROR.value()) {
      if (status == HttpStatus.OK) {
        Counter.builder("canary.invocations.statusCode.200").register(registry).increment();
        return true;

      } else {
        Counter.builder("canary.invocations.statusCode.4xx").register(registry).increment();
      }
    }

    Counter.builder("canary.invocations.statusCode.5xx").register(registry).increment();
    return false;
  }

  /** Create a test action with default values when application starts */
  protected void createAction() {
    try {
      Action action = new Action(namespace, actionName, new Exec(actionKind, actionCode));

      HttpEntity<Action> entity = new HttpEntity<Action>(action, headers);

      restTemplate.exchange(url + "?overwrite=true", HttpMethod.PUT, entity, String.class);

    } catch (Exception ex) {
      LOG.error("Invocation creation failed: " + ex.getLocalizedMessage());
    }
  }

  /**
   * Invoke action
   *
   * @return HttpStatus
   */
  protected HttpStatus getInvocationStatusCode() {
    HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

    try {
      HttpEntity<String> entity = new HttpEntity<String>("", headers);

      ResponseEntity<String> responseEntity =
          restTemplate.exchange(
              url + "?blocking=true&result=true", HttpMethod.POST, entity, String.class);

      statusCode = responseEntity.getStatusCode();

    } catch (HttpStatusCodeException ex) {
      statusCode = ex.getStatusCode();

    } catch (Exception e) {
      LOG.error("Action invocation failed: " + e.getLocalizedMessage());
    }

    return statusCode;
  }

  public boolean isHealthy() {
    return serviceHealthy;
  }

  private void setHealthy(boolean serviceHealthy) {
    this.serviceHealthy = serviceHealthy;
  }
}

class BasicCredentials {
  private String username;
  private String password;

  public BasicCredentials(String auth) {
    setCredentials(auth);
  }

  // Split `auth` filed into username and password
  private void setCredentials(String auth) {
    String[] authArr = auth.split(":");
    setUsername(authArr[0]);
    setPassword(authArr[1]);
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  private void setUsername(String username) {
    this.username = username;
  }

  private void setPassword(String password) {
    this.password = password;
  }
}
