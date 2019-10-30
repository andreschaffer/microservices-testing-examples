[![Build Status](https://travis-ci.org/andreschaffer/microservices-testing-examples.svg?branch=master)](https://travis-ci.org/andreschaffer/microservices-testing-examples)
[![Coverage Status](https://coveralls.io/repos/github/andreschaffer/microservices-testing-examples/badge.svg?branch=master)](https://coveralls.io/github/andreschaffer/microservices-testing-examples?branch=master)
# Microservices Testing Examples

# Strategy
When it comes to testing microservices, usually there are two alternatives:  
a) Deploy all of them and test them in an end-to-end fashion  
b) Mock external dependencies in unit / integration tests

The problem with alternative _a_ is that it doesn't scale. It gets only harder to maintain the tests as the system evolves and new microservices arise.  
The problem with alternative _b_ is that the mocks might not behave the same way as the real dependencies, 
and thus we might miss integration problems.

So, how to proceed? Glad you asked.  
This project will focus on [Consumer Driven Contract Testing](http://martinfowler.com/articles/consumerDrivenContracts.html) to overcome those limitations. 
It is a technique based on mocks, so that we benefit from fast feedback and no scalability issues, that attacks
the problem of potential incompatible behavior by recording the interactions with the mocks 
and then allowing the real services to test that they behave the same way the mock did instead.

Some of the tools that support Consumer Driven Contract Testing are:
[Pact](https://docs.pact.io/),
[Pacto](http://thoughtworks.github.io/pacto/)
and [Spring Cloud Contract](https://cloud.spring.io/spring-cloud-contract/spring-cloud-contract.html).
This project will use Pact.

# Microservices
The microservices involved in this project are:  
- The special-membership-service, that manages members of a special membership;  
- The credit-score-service, that holds information about individuals credit scores;  
- The welcome-member-email-service, that contacts new members with a welcome email.

The system flow is very simple: 
a special membership request comes at the special-membership-service that in turn 
looks up the individual credit score at the credit-score-service 
and then decides whether it should create the membership or not. 
As a result of a new membership, the special-membership-service publishes a corresponding event that is picked by
the welcome-member-email-service that then contacts the new member with a warm welcome.

# Pact Broker
While testing, the way we'll make the interactions records (called pacts from now on) available to the real services 
is through a pact broker. We can run it with [Docker Compose](https://docs.docker.com/compose/) and access it on a browser [(http://localhost)](http://localhost).

```bash
docker-compose -f pact-tools/pact-broker/docker-compose.yml up -d
```
  
We will also need to build the [pact cli](https://github.com/pact-foundation/pact_broker-client) tool to interact with the broker.
```bash
docker build -t pact-cli pact-tools/pact-cli
```

# Running the tests
We can run all the flows with [Maven](https://maven.apache.org/) and the pact cli like this:

For the welcome-member-email-service, we build, create its pacts, publish and tag them:
```bash
mvn clean verify -pl welcome-member-email-service -Pcode-coverage
mvn verify -pl welcome-member-email-service -Pconsumer-pacts
docker run --rm --net host -v `pwd`/welcome-member-email-service/target/pacts:/target/pacts pact-cli publish /target/pacts --consumer-app-version `git rev-parse --short HEAD` --broker-base-url localhost --broker-username=rw_user --broker-password=rw_pass
docker run --rm --net host pact-cli create-version-tag --pacticipant welcome-member-email-service --version `git rev-parse --short HEAD` --tag prod --broker-base-url localhost --broker-username=rw_user --broker-password=rw_pass
```
  
For the special-membership-service, we build, verify consumers' pacts, create its own pacts, publish and tag both the verification and the pacts created:
```bash
mvn clean verify -pl special-membership-service -Pcode-coverage
mvn verify -pl special-membership-service -Pprovider-pacts -Dpact.verifier.publishResults=true -Dpact.provider.version=`git rev-parse --short HEAD` -Dpactbroker.tags=prod -Dpactbroker.user=rw_user -Dpactbroker.pass=rw_pass
mvn verify -pl special-membership-service -Pconsumer-pacts
docker run --rm --net host -v `pwd`/special-membership-service/target/pacts:/target/pacts pact-cli publish /target/pacts --consumer-app-version `git rev-parse --short HEAD` --broker-base-url localhost --broker-username=rw_user --broker-password=rw_pass
docker run --rm --net host pact-cli create-version-tag --pacticipant special-membership-service --version `git rev-parse --short HEAD` --tag prod --broker-base-url localhost --broker-username=rw_user --broker-password=rw_pass
```
  
For the credit-score-service, we build, verify consumers' pacts and tag the verification:
```bash
mvn clean verify -pl credit-score-service -Pcode-coverage
mvn verify -pl credit-score-service -Pprovider-pacts -Dpact.verifier.publishResults=true -Dpact.provider.version=`git rev-parse --short HEAD` -Dpactbroker.tags=prod -Dpactbroker.user=rw_user -Dpactbroker.pass=rw_pass
docker run --rm --net host pact-cli create-version-tag --pacticipant credit-score-service --version `git rev-parse --short HEAD` --tag prod --broker-base-url localhost --broker-username=rw_user --broker-password=rw_pass
```

## Test separation
We created two auxiliary maven profiles to hold control of creating the pacts (consumer-pacts) 
and verifying the pacts (provider-pacts).  

We separate the pact tests from the other tests. Pact tests are focused on the contracts and shouldn't be abused, 
otherwise we'll give the providers a hard time verifying an explosion of interactions.  

We have many integration tests with regular mocks for the expected behavior of our services in different scenarios
and a few pact tests in their own package (\*.pacts). In each pact test we focus on one provider integration contract at a time, 
specifying only the properties that we need and using appropriate matchers. (Side note: since it's a point-to-point integration we are talking about, 
we could use pact with unit tests - important to make sure the client used is the same one the service uses. We opted for 
slim integration tests instead since the services are very small anyway).  

The pact verification tests also have their own package (\*.pacts.verifications). Here we need to be able to setup the different states the consumers specify in their interactions 
and it becomes more evident that we shouldn't abuse pact in order to avoid unnecessary verifications at this point. (Side note: the class names are following a different pattern (\*PactVerifications) 
that is aligned with the maven profile (provider-pacts) just so we get a better control of when to run them - similar control could be achieved with jUnit categories as well).  

Now it's time for you to go ahead and take a look at those tests! Try changing a contract and see the tests fail :)

## Dependencies graph
Visit the pact broker page again after running the tests and check the pacts are there together with a cool dependencies graph:  
![alt text](https://github.com/andreschaffer/microservices-testing-examples/blob/master/docs/images/pact_broker_dependencies_graph.png "Pact broker dependencies graph")

# Automating it all with pipelines
                                            (Project A pipeline)


    +-------+    +--------------+    +--------------+    +---------------+    +--------+    +-------------+
    |       |    |              |    |              |    |               |    |        |    |  Tag Pacts  |
    | Build | +> | Verify Pacts | +> | Create Pacts | +> | Can I Deploy? | +> | Deploy | +> |     as      |
    |       |    |              |    |              |    |               |    |        |    |    Prod     |
    +-------+    +--------------+    +--------------+    +---------------+    +--------+    +-------------+
                    |                   |                             |                                |
                    |                   |                             |                                |
                    |                   |                             |                                |
                    |                   |  2    +-------------+    5  |                                |
                    |                   +-------+             +-------+                                |
                    |  1                        | Pact Broker |                                     6  |
                    +---------------------------+             +----------------------------------------+
                                                +-------------+
                                                   |       |
                            +----------------------+       +---------------------+
                            |  3                                              4  |
                            |                                                    |
                            |             (Project B Support Pipeline)           |
                            |                                                    |
                            |                                                    |
                            |    +-----------------------+    +--------------+   |
                            |    |                       |    |              |   |
                            +----+ Checkout Prod Version | +> | Verify Pacts +---+
                                 |                       |    |              |
                                 +-----------------------+    +--------------+


When a change is pushed to Project A repo, its pipeline is triggered:
* **Build:** checkout, package and run the regular unit and integration tests.
* **Verify Pacts:** download its consumers' pacts (tagged as prod) from the pact broker, verify all of them and publish the results to the pact broker.
* **Create Pacts:** create its pacts and publish them to the pact broker.

The pact broker will trigger all provider pipelines that has a contract with Project A as consumer:

* **Checkout Prod Version:** checkout Project B code corresponding to its prod tag.
* **Verify Pacts:** download the pacts that Project A created with B, verify them and publish the results to the pact broker.

Meanwhile, the pipeline of Project A was hanging in the **Can I Deploy?** until the pacts it created were marked as verified in the pact broker and resumes:
* **Deploy:** with confidence that it can interact with its neighbours, we can deploy Project A to production.
* **Tag Pacts as Prod:** tag all its pacts and verifications as prod in the pact broker.

**Disclaimer**: You can see these building blocks in our travis file, but the flow looks a little bit different whereas the provider support pipelines are simulated.


# Contributing
If you would like to help making this project better, see the [CONTRIBUTING.md](CONTRIBUTING.md).  

# Maintainers
Send any other comments, flowers and suggestions to [André Schaffer](https://github.com/andreschaffer) and [Dan Eidmark](https://github.com/daneidmark).

# License
This project is distributed under the [MIT License](LICENSE).
