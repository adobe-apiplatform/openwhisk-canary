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

package com.adobe.io.runtime.canary.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpResponse.response;

import com.adobe.io.runtime.canary.BaseTest;
import com.adobe.io.runtime.canary.service.InvocationsService;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

public class HealthControllerTest extends BaseTest {

  @LocalServerPort private int port;

  private String url;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private InvocationsService invocationsService;

  @Value("${invokers.endpoint}")
  private String invokersEndpoint;

  @Value("${whisk.action.endpoint}")
  private String invocationsEndpoint;

  @Before
  public void initUrl() {
    url = "http://localhost:" + port + "/status";

    // Create action
    mockInvocationsClient(HttpMethod.PUT, invocationsEndpoint, 200, "{}", 0, 1);
    invocationsService.init();
  }

  @Test
  public void testHealth_serviceIsHealthy() throws Exception {

    // Invokers: Healthy - 1 Invokers UP
    mockInvokersClient(
        HttpMethod.GET,
        invokersEndpoint,
        200,
        "{\"invoker3/invoker-a.34a2afd4-7239-11e9-91a4-be284c1d85f1.1\":\"up\"}",
        0,
        1);

    // Invocations: Healthy - One invocation responding with 200
    mockInvocationsClient(HttpMethod.POST, invocationsEndpoint, 200, "{}", 0, 1);

    Thread.sleep(1000);

    RequestEntity<Void> req = RequestEntity.post(new URI(url)).build();
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, req, String.class);

    assertThat(response.getBody()).isEqualTo("{\"invokers\":\"up\",\"invocations\":\"up\"}");
    assertThat(response.getStatusCodeValue()).isEqualTo(200);
  }

  @Test
  public void testHealth_inovkerServiceIsHealth() throws Exception {

    // Invokers: Healthy - 1 Invokers UP
    mockInvokersClient(
        HttpMethod.GET,
        invokersEndpoint,
        403,
        "{\"invoker3/invoker-a.34a2afd4-7239-11e9-91a4-be284c1d85f1.1\":\"up\"}",
        0,
        1);

    // Invocations: Healthy - One invocation responding with 200
    mockInvocationsClient(HttpMethod.POST, invocationsEndpoint, 200, "{}", 0, 1);

    Thread.sleep(1000);

    RequestEntity<Void> req = RequestEntity.post(new URI(url)).build();
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, req, String.class);

    assertThat(response.getBody()).isEqualTo("{\"invokers\":\"down\",\"invocations\":\"up\"}");
    assertThat(response.getStatusCodeValue()).isEqualTo(503);
  }

  @Test
  public void testHealth_unhealthy() throws Exception {

    // Invokers: Unhealthy - All invokers are down
    mockInvokersClient(
        HttpMethod.GET,
        invokersEndpoint,
        200,
        "{\"invoker3/invoker-a.34a2afd4-7239-11e9-91a4-be284c1d85f1.1\":\"down\","
            + "\"invoker0/invoker-a.34a261b2-7239-11e9-91a4-be284c1d85f1.1\":\"down\","
            + "\"invoker2/invoker-a.34a21390-7239-11e9-91a4-be284c1d85f1.1\":\"down\","
            + "\"invoker5/invoker-a.379e8ab8-7239-11e9-91a4-be284c1d85f1.1\":\"down\","
            + "\"invoker4/invoker-a.34a34c16-7239-11e9-91a4-be284c1d85f1.1\":\"down\","
            + "\"invoker1/invoker-a.34a1c56e-7239-11e9-91a4-be284c1d85f1.1\":\"down\"}",
        0,
        2);

    // Invocations: Unhealthy - Responded with `503`
    mockInvocationsClient(HttpMethod.POST, invocationsEndpoint, 503, "{}", 0, 2);

    Thread.sleep(2000);

    RequestEntity<Void> req = RequestEntity.post(new URI(url)).build();
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, req, String.class);

    assertThat(response.getBody()).isEqualTo("{\"invokers\":\"down\",\"invocations\":\"down\"}");
    assertThat(response.getStatusCodeValue()).isEqualTo(503);
  }
}
