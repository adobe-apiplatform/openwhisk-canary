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

package com.adobe.io.runtime.canary.config;

import javax.net.ssl.HttpsURLConnection;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

  @Value("${http.client.read.timeout}")
  private Integer httpClientTimeout;

  @Bean
  public RestTemplate restTemplate() {
    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory(httpClient);
    requestFactory.setConnectTimeout(httpClientTimeout);
    requestFactory.setReadTimeout(httpClientTimeout);
    return new RestTemplate(requestFactory);
  }
}
