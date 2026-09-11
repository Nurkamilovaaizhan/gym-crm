Feature: Training component

  Scenario: Create training successfully
    Given training component is ready
    When I submit valid training
    Then training is saved successfully

  Scenario: Create training fails when trainer is missing
    Given training component is ready
    And trainer does not exist
    When I submit training
    Then training error is returned