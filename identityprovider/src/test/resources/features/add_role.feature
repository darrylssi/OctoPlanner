
Feature: I can add a role to a user
  Scenario: Add a new role to a user
    Given There is a user with the role "STUDENT"
    When I add the role "TEACHER" to the user
    Then The user has the role "TEACHER"
    And The user has the role "STUDENT"
    And The user has 2 roles

  Scenario: Add the same role to a user
    Given There is a user with the role "STUDENT"
    When I add the role "STUDENT" to the user
    Then The user has 1 roles