#Copyright 2019 Adobe. All rights reserved.
#This file is licensed to you under the Apache License, Version 2.0 (the "License");
#you may not use this file except in compliance with the License. You may obtain a copy
#of the License at http://www.apache.org/licenses/LICENSE-2.0

#Unless required by applicable law or agreed to in writing, software distributed under
#the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
#OF ANY KIND, either express or implied. See the License for the specific language
#governing permissions and limitations under the License.

FROM adoptopenjdk/openjdk8:x86_64-alpine-jdk8u172-b11

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

ENV UID=1001 \
    NOT_ROOT_USER=owuser

RUN apk add --update sed bash

# Copy app jars
ADD build/libs/canary-*.jar /application.jar

RUN adduser -D -u ${UID} -h /home/${NOT_ROOT_USER} -s /bin/bash ${NOT_ROOT_USER}
USER ${NOT_ROOT_USER}

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/application.jar"]
