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

import com.adobe.io.runtime.canary.BaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

public class InvocationsServiceTest extends BaseTest {

  @Autowired RestTemplate restTemplate;

  @Value("${whisk.action.endpoint}")
  private String actionEndpoint;

  @Autowired private InvocationsService service;

  @Before
  public void initCreation() throws Exception {

    // Create action
    mockInvocationsClient(HttpMethod.PUT, actionEndpoint, 200, "{}", 0, 1);
    service.init();
  }

  @Test
  public void testStatusOk() {
    assert (service.isStatusOk(HttpStatus.OK));
    assert (!service.isStatusOk(HttpStatus.NOT_FOUND));
    assert (!service.isStatusOk(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  @Test
  public void testInvocationService_variousResponses() throws Exception {

    // Unhealthy: Response is timing out
    mockInvocationsClient(HttpMethod.POST, actionEndpoint, 200, "{}", 200, 2);
    Thread.sleep(2000);
    assert (!service.isHealthy());

    // Healthy: Invocations return 200
    mockInvocationsClient(HttpMethod.POST, actionEndpoint, 200, "{}", 0, 1);
    Thread.sleep(1000);
    assert (service.isHealthy());

    // Unhealthy: Invocations return 404
    mockInvocationsClient(HttpMethod.POST, actionEndpoint, 404, "{}", 0, 2);

    Thread.sleep(2000);
    assert (!service.isHealthy());

    // Healthy: Invocations return 200 again
    mockInvocationsClient(HttpMethod.POST, actionEndpoint, 200, "{}", 0, 1);

    Thread.sleep(1000);
    assert (service.isHealthy());

    // Unhealthy: Invocations return 502
    mockInvocationsClient(HttpMethod.POST, actionEndpoint, 502, "{}", 0, 2);

    Thread.sleep(2000);
    assert (!service.isHealthy());
  }
}
