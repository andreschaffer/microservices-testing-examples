#!/bin/bash

docker run --net=host -v `pwd`/welcome-member-email-service/target/pacts:/target/pacts pact-cli publish /target/pacts --broker-base-url=localhost --consumer-app-version=1.0-SNAPSHOT
docker run --net=host -v `pwd`/special-membership-service/target/pacts:/target/pacts pact-cli publish /target/pacts --broker-base-url=localhost --consumer-app-version=1.0-SNAPSHOT
