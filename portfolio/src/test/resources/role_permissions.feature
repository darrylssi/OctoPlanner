# TODO do you need to be an admin to do this?
# How would we check for that?
Feature: I can only edit roles if I am a teacher or course administrator
  Scenario: Add a new role to a user as a teacher
    Given There is a user with the role "STUDENT"
    And I have the role "TEACHER"
    When I try to add the role "TEACHER" to the user
    Then I get an "Okay" response
    And I get the message "Role TEACHER added"

  Scenario: Add a new role to a user as a course administrator
    Given There is a user with the role "STUDENT"
    And I have the role "COURSE_ADMINISTRATOR"
    When I add the role "TEACHER" to the user
    Then I get an "Okay" response
    And I get the message "Role TEACHER added"

  Scenario: Add a new role to a user as a student
    Given There is a user with the role "STUDENT"
    And I have the role "STUDENT"
    When I add the role "TEACHER" to the user
    Then I get a "Forbidden" response
    And I get the message "You do not have permission to edit roles"

  Scenario: Delete a role from a user with several roles as a teacher
    Given There is a user with the roles "STUDENT" and "TEACHER"
    And I have the role "TEACHER"
    When I delete the role "TEACHER" from the user
    Then I get an "Okay" response
    And I get the message "Role TEACHER removed"


    #these two might not be able to be mocked?
  Scenario: I cannot delete the last role from a user
    Given There is a user with the role "TEACHER"
    When I delete the role "TEACHER" from the user
    Then I get a "Bad request" response
    And I get the message "A user cannot have zero roles"

  Scenario: Add the same role to a user
    Given There is a user with the role "STUDENT"
    And I have the role "TEACHER"
    When I add the role "STUDENT" to the user
    Then I get a "Bad request" response
    And I get the message "You cannot have duplicate roles"