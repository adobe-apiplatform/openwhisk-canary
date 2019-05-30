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

package com.adobe.io.runtime.canary.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Status {
  private String invokers = "down";
  private String invocations = "down";
  @JsonIgnore private boolean healthy = false;

  public Status(Boolean invokersHealth, Boolean invocationsHealth) {
    if (invokersHealth) {
      setInvokers("up");
    }

    if (invocationsHealth) {
      setInvocations("up");
    }

    if (invokersHealth && invocationsHealth) {
      setHealthy(true);
    }
  }

  public String getInvokers() {
    return invokers;
  }

  public void setInvokers(String invokers) {
    this.invokers = invokers;
  }

  public String getInvocations() {
    return invocations;
  }

  public void setInvocations(String invocations) {
    this.invocations = invocations;
  }

  public boolean isHealthy() {
    return healthy;
  }

  public void setHealthy(boolean healthy) {
    this.healthy = healthy;
  }
}
