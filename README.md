# Openwhisk Canary Monitoring

[![Build Status](https://travis-ci.org/adobe-apiplatform/openwhisk-canary.svg?branch=master)](https://travis-ci.org/adobe-apiplatform/openwhisk-canary)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![codecov](https://codecov.io/gh/adobe-apiplatform/openwhisk-canary/branch/master/graph/badge.svg)](https://codecov.io/gh/adobe-apiplatform/openwhisk-canary)

This service provides a health status for the cluster where the openwhisk framework is running by monitoring various components. 

## Design
This services will check at predefined intervals the following components:
> Consider the system unhealthy only if any of the defined components is unhealthy.  
### 1. Invokers 
Check the number of available invokers to be above a configurable threshold. Read the number of available invokers from `/invokers` endpoint, and compare it against the invokers marked as **up**.
#### Configuration
- `invokers.interval.check = 0/1 * * * * *` Cron like schedule of invoker check (runs every second)
- `invokers.threshold.percentage = 20` Lower **%** threshold of **up** invokers (20% of invokers should be up for a healthy state)
- `invokers.maxfailures = 20` Number of repeated failures allowed until service is considered unavailable (service will be considered unavailable after 20 sequential unhealthy responses)

### 2. Invocations 
Trigger blocking invocations and check the HTTP response.
> Interpret only `200` status code as being successful response.

#### Configuration
- `invocations.interval.check = 0/1 * * * * *` Cron like schedule for invocations triggering (runs every second)
- `invocations.maxfailures = 20` Number of repeated failures allowed until service is considered unavailable (service will be considered unavailable after 20 sequential unhealthy responses)

## Build
This command pulls the docker images for local testing and development:

```bash
make all
```

## Run
>First configure and run `openwhisk docker-compose` that can be found in the [openwhisk-tools][1] project.

Once the `openwhisk docker-compose` has been started go ahead and execute the following command: 
```bash
make run
``` 

This will start the `openwhisk-canary` service. These ports must be available:

- `8086` - openwhisk-canary service canary health 
- `8087` - openwhisk-canary service actuator
> In a production environment, this application should be run inside the OW cluster behind the API-Gateway.

## Status
After starting the service, the status can be monitored using this endpoint:
`http://localhost:8086/status`
This will respond with the following status codes:
- `200` in case at least one monitored components is working fine
- `503` in case all monitored components are failing

## Monitoring 
This microservice has both **Prometheus** and **Datadog** support. The fallowing metrics are available: 
- `canary.invocations.statusCode.200{service="canary",} 1.0` Counter for the number of invocations that responded with `200` http code
- `canary.invocations.statusCode.4xx{service="canary",} 1.0` Counter for the number of invocations that responded with `4xx` http code
- `canary.invocations.statusCode.5xx{service="canary",} 1.0` Counter for the number of invocations that responded with `5xx` http code


- `canary.service.healthy.responses{service="canary",} 1.0` Counter with the healthy service inquiries 
- `canary.service.unhealthy.responses{service="canary",} 1.0` Counter with the unhealthy service inquiries


Metric tags can be added using this property:
`service.tags=service,canary` Every element in the list will be interpret in a group of two as `tag name` and `tag value`. 

For example: `service.tags=service,canary,region,ue1` will create this metric pattern `canary.invocations.statusCode.200{service="canary",region="ue1"}`
 

### Prometheus
Metrics endpoint is running on port `8087` and can be accessed with this endpoint `/actuator/prometheus`

### Datadog
`management.metrics.export.datadog.api-key` property has to be configured with the Datadog account's `api-key` 

### Health check
Service health check is provided by the `actuator` and should be used with various health checks (ie. marathon)
- http://localhost:8087/actuator/health


[1]: https://github.com/apache/incubator-openwhisk-devtools/tree/master/docker-compose 