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
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

public class InvokerServiceTest extends BaseTest {

  @Autowired InvokersService service;

  @Value("${invokers.endpoint}")
  private String invokersEndpoint;

  @Test
  public void testValidateThreashold() {
    InvokersService service = new InvokersService();
    assert (service.validateThreshold(10, 5, 20));
    assert (!service.validateThreshold(10, 1, 20));
    assert (!service.validateThreshold(0, 0, 20));
  }

  @Test
  public void testInokerRestCall_variousResponses() throws Exception {

    // Unhealthy: Response is timing out
    mockInvokersClient(
        HttpMethod.GET,
        invokersEndpoint,
        200,
        "{\"invoker3/invoker-a.34a2afd4-7239-11e9-91a4-be284c1d85f1.1\":\"up\"}",
        200,
        2);

    Thread.sleep(2000);
    assert (!service.isHealthy());

    // Healthy: Server responds with 100% success (1 invoker up)
    mockInvokersClient(
        HttpMethod.GET,
        invokersEndpoint,
        200,
        "{\"invoker3/invoker-a.34a2afd4-7239-11e9-91a4-be284c1d85f1.1\":\"up\"}",
        0,
        1);

    Thread.sleep(1000);
    assert (service.isHealthy());

    // Unhealthy: Only one invoker in 6 is available (17%)
    mockInvokersClient(
        HttpMethod.GET,
        invokersEndpoint,
        200,
        "{\"invoker3/invoker-a.34a2afd4-7239-11e9-91a4-be284c1d85f1.1\":\"up\","
            + "\"invoker0/invoker-a.34a261b2-7239-11e9-91a4-be284c1d85f1.1\":\"down\","
            + "\"invoker2/invoker-a.34a21390-7239-11e9-91a4-be284c1d85f1.1\":\"down\","
            + "\"invoker5/invoker-a.379e8ab8-7239-11e9-91a4-be284c1d85f1.1\":\"down\","
            + "\"invoker4/invoker-a.34a34c16-7239-11e9-91a4-be284c1d85f1.1\":\"down\","
            + "\"invoker1/invoker-a.34a1c56e-7239-11e9-91a4-be284c1d85f1.1\":\"down\"}",
        0,
        2);

    Thread.sleep(2000);
    assert (!service.isHealthy());

    // Healthy: All 6 invokers ar back up
    mockInvokersClient(
        HttpMethod.GET,
        invokersEndpoint,
        200,
        "{\"invoker3/invoker-a.34a2afd4-7239-11e9-91a4-be284c1d85f1.1\":\"up\","
            + "\"invoker0/invoker-a.34a261b2-7239-11e9-91a4-be284c1d85f1.1\":\"up\","
            + "\"invoker2/invoker-a.34a21390-7239-11e9-91a4-be284c1d85f1.1\":\"up\","
            + "\"invoker5/invoker-a.379e8ab8-7239-11e9-91a4-be284c1d85f1.1\":\"up\","
            + "\"invoker4/invoker-a.34a34c16-7239-11e9-91a4-be284c1d85f1.1\":\"up\","
            + "\"invoker1/invoker-a.34a1c56e-7239-11e9-91a4-be284c1d85f1.1\":\"up\"}",
        0,
        1);

    Thread.sleep(1000);
    assert (service.isHealthy());

    // Unhealthy: Invalid response
    mockInvokersClient(HttpMethod.GET, invokersEndpoint, 200, "invalid response", 0, 2);

    Thread.sleep(2000);
    assert (!service.isHealthy());
  }
}
