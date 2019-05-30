/*
Copyright 2018 Adobe. All rights reserved.
This file is licensed to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
 */

package com.adobe.io.runtime.canary;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseTest {

  private static ClientAndServer mockServerInvocations;
  private static ClientAndServer mockServerInvokers;
  private static int invocationsServerPort = 8222;
  private static int invokersServerPort = 8223;

  @BeforeClass
  public static void init() {
    mockServerInvocations = startClientAndServer(invocationsServerPort);
    mockServerInvokers = startClientAndServer(invokersServerPort);
  }

  @AfterClass
  public static void stopServer() {
    mockServerInvocations.stop();
    mockServerInvokers.stop();
  }

  @Test
  public void base() {}

  protected void mockInvocationsClient(
      HttpMethod method, String path, int status, String body, int delay, int times) {
    mockClient(invocationsServerPort, method, path, status, body, delay, times);
  }

  protected void mockInvokersClient(
      HttpMethod method, String path, int status, String body, int delay, int times) {
    mockClient(invokersServerPort, method, path, status, body, delay, times);
  }

  private void mockClient(
      int port, HttpMethod method, String path, int status, String body, int delay, int times) {
    new MockServerClient("127.0.0.1", port)
        .when(request().withMethod(method.toString()).withPath(path), Times.exactly(times))
        .respond(
            response()
                .withStatusCode(status)
                .withHeader(new Header("Content-Type", "application/json; charset=utf-8"))
                .withBody(body)
                .withDelay(TimeUnit.MILLISECONDS, delay));
  }
}
