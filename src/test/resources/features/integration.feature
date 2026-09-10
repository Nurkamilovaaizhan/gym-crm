@integration
Feature: Microservices integration

  Scenario: Training is transferred to workload service
    Given both microservices are available
    When I create a 60 minutes training for trainer "Test.Trainer"
    Then workload for trainer "Test.Trainer" contains 60 minutes

  Scenario: Invalid training is rejected
    Given both microservices are available
    When I create an invalid training
    Then main service returns a client error