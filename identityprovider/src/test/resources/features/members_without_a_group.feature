Feature: The Members Without A Group group always exists
  Scenario: The Members Without A Group group is created automatically
    When I try to access the Members Without A Group group
    Then Members Without A Group can be accessed

  Scenario: The Members Without A Group group cannot be deleted
    When I try to delete the Members Without A Group group
    Then the request to delete Members Without A Group fails