[![Build Status](https://travis-ci.org/andreschaffer/microservices-testing-examples.svg?branch=master)](https://travis-ci.org/andreschaffer/microservices-testing-examples)
[![Coverage Status](https://coveralls.io/repos/github/andreschaffer/microservices-testing-examples/badge.svg?branch=master)](https://coveralls.io/github/andreschaffer/microservices-testing-examples?branch=master)
# Microservices Testing Examples

# Strategy
When it comes to testing microservices, there are two alternatives:  
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
docker-compose up -d
```

# Running the tests
We can run all the tests with [Maven](https://maven.apache.org/) like this:
```bash
mvn clean verify -Pupload-pacts,verify-pacts
```

### Behind the curtains
We created two auxiliary maven profiles to hold control of uploading the pacts to the broker (upload-pacts) 
and downloading the pacts from the broker / running the pact verification tests (verify-pacts).  
Here the welcome-email-service is built first and publishes the pact with special-membership-service;  
Special-membership-service verifies that pact and publishes its own pact with credit-score-service;  
Credit-score-service finally verifies the pact, concluding the chain.

### Test separation
Here we separate the pact tests from the other tests. Pact tests are focused on the contracts and shouldn't be abused, otherwise we'll give the providers a hard time verifying an explosion of interactions.  

We have many integration tests with regular mocks for the expected behavior of our services in different scenarios
and a few pact tests in their own package (\*.pacts). In each pact test we focus on one provider integration contract at a time, specifying only the properties that we need and using appropriate matchers. (Side note: since it's a point-to-point integration we are talking about, we could use pact with unit tests - important to make sure the client used is the same one the service uses. We opted for slim integration tests instead since the services are very small anyway).  

The pact verification tests also have their own package (\*.pacts.dependents). Here we need to be able to setup the different states the consumers specify in their interactions and it becomes more evident that we shouldn't abuse pact in order to avoid unnecessary verifications at this point. (Side note: the class names are following a different pattern (\*PactsVerifications) that is aligned with the maven profile (verify-pacts) just so we get a better control of when to run them - similar control can be achieved with jUnit categories as well).  

Now it's time for you to go ahead and take a look at those tests! Try changing a contract and see the tests fail :)

### Dependencies graph
Visit the pact broker page again after running the tests and check the pacts are there together with a cool dependencies graph:  
![alt text](https://github.com/andreschaffer/microservices-testing-examples/blob/master/docs/images/pact_broker_dependencies_graph.png "Pact broker dependencies graph")

# Contributing
If you would like to help making this project better, see the [CONTRIBUTING.md](CONTRIBUTING.md).  

# Maintainers
Send any other comments, flowers and suggestions to [André Schaffer](https://github.com/andreschaffer).

# License
This project is distributed under the [MIT License](LICENSE).
