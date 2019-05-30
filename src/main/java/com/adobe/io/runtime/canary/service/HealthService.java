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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class HealthService {
  private static Logger LOG = LoggerFactory.getLogger(HealthService.class);

  private Integer failedOccurrences = 0;

  protected int counter = 0;

  protected boolean isServiceHealthy(boolean healthyResponse, int maxFailures) {

    boolean healthyService = true;

    if (healthyResponse) {
      if (failedOccurrences >= maxFailures) {
        LOG.info("HEALTHY after failed occurrences: " + failedOccurrences);
      }

      failedOccurrences = 0;
      healthyService = true;
    }

    if (!healthyResponse) {
      failedOccurrences++;
    }

    if (failedOccurrences >= maxFailures) {
      healthyService = false;

      if (failedOccurrences == maxFailures) {
        LOG.info("UNHEALTHY after failed occurrences: " + failedOccurrences);
      }
    }

    counter++;
    return healthyService;
  }

  protected int getFailedOccurrences() {
    return this.failedOccurrences;
  }
}
