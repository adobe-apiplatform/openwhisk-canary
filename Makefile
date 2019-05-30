.PHONY: docker
docker:
	docker build -t adobeapiplatform/openwhisk-canary .

.PHONY: gradle-build
gradle-build:
	./gradlew goJF build jacocoTestCoverageVerification

.PHONY: docker-build
docker-build: gradle-build docker

.PHONY: clean
clean:
	gradle clean

.PHONY: all
all: clean gradle-build docker

.PHONY: run
run: check-required-ports start-docker-compose

.PHONY: start-docker-compose
start-docker-compose:
	docker-compose  up

.PHONY: stop
stop:
	docker-compose stop

.PHONY: rm
rm:
	docker-compose rm -f

.PHONY: check-required-ports
check-required-ports:
	echo "checking required ports ... "
	@occupiedports=0; \
	for port in 9096; do \
		pid=`lsof -Pi :$$port -sTCP:LISTEN -t` ; \
		if [ ! -z "$$pid" ];  then let "occupiedports+=1" ; echo "$$(tput setaf 1)Port $$port is taken by PID:$$pid.$$(tput sgr0)"; fi; \
	done; \
	if [ "$$occupiedports" = 0 ]; then \
		echo " ... OK"; \
	else \
		echo "$$(tput setaf 2)Ports occupied. To stop openwhisk use: $$(tput setaf 4)make destroy$$(tput setaf 2) or: $$(tput setaf 4)make stop$$(tput sgr0)"; \
		exit 1; \
	fi

.PHONY: report
report:
	./gradlew jacocoTestReport