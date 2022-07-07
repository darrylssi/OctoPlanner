
Feature: I can remove a role from a user
  Scenario: Delete a role from a user with several roles
    Given There is a user with the role "STUDENT"
    And I add the role "TEACHER" to the user
    When I delete the role "TEACHER" from the user
    Then The user has the role "STUDENT"
    And The user has 1 roles

  Scenario: I cannot delete the last role from a user
    Given There is a user with the role "TEACHER"
    When I delete the role "TEACHER" from the user
    Then The user has the role "TEACHER"
    And The user has 1 roles