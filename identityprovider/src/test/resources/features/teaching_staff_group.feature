Feature: The Teaching Staff group always exists
  Scenario: Teaching Staff group is created automatically
    When I try to access the Teaching Staff group
    Then Teaching Staff can be accessed

  Scenario: The Teaching Staff group cannot be deleted
    When I try to delete the Teaching Staff group
    Then the request to delete Teaching Staff fails

  Scenario: The Teaching Staff group cannot be edited
    When I try to edit the Teaching Staff group
    Then the request to edit Teaching Staff fails