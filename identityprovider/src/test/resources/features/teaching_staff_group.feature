Feature: The Teaching Staff group always exists
  Scenario: Teaching Staff group is created automatically
    When I try to access the Teaching Staff group
    Then it can be accessed

  Scenario: The Teaching Staff group cannot be deleted
    When I try to delete the Teaching Staff group
    Then the request fails