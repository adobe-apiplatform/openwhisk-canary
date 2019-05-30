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

import com.adobe.io.runtime.canary.model.Status;
import com.adobe.io.runtime.canary.service.InvocationsService;
import com.adobe.io.runtime.canary.service.InvokersService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/")
public class HealthController {

  private static Logger LOG = LoggerFactory.getLogger(HealthController.class);

  @Autowired InvokersService invokers;
  @Autowired InvocationsService invocations;

  @Autowired MeterRegistry registry;

  @GetMapping(path = "/status")
  public ResponseEntity canaryCheck() {

    // Return unavailable if one of the components is unhealthy
    Status status = new Status(invokers.isHealthy(), invocations.isHealthy());
    if (status.isHealthy()) {
      Counter.builder("canary.service.healthy.responses").register(registry).increment();
      return new ResponseEntity(status, HttpStatus.OK);
    }

    Counter.builder("canary.service.unhealthy.responses").register(registry).increment();
    return new ResponseEntity(status, HttpStatus.SERVICE_UNAVAILABLE);
  }
}
