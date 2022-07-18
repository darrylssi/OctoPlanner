Feature: The project's dates are changed

  Scenario Outline:
    Given The project has the following sprints with dates
      | 2022-01-01  | 2022-02-02  |
      | 2022-02-06  | 2022-03-04  |
    And The projects creation date is "2022-01-01"
    When I set the project start date to <StartDate>
    And I set the project end date to <EndDate>
    Then Project <Outcome> message should be displayed

    Examples:
      | StartDate  | EndDate    | Outcome |
      | "2022-02-04" | "2022-08-05" | "Sprint 1: 01/Jan/2022 - 02/Feb/2022 is outside the project dates" |
      | "2022-01-20" | "2022-08-20" | "Sprint 1: 01/Jan/2022 - 02/Feb/2022 is outside the project dates" |
      | "2022-01-01" | "2022-02-10" | "Sprint 2: 06/Feb/2022 - 04/Mar/2022 is outside the project dates" |
      | "2022-01-02" | "2022-01-01" | "Start date must always be before end date" |
      | "2020-01-01" | "2022-03-04" | "Project cannot be set to start more than a year before it was created (cannot start before 01/Jan/2021)" |
      | "2022-01-01" | "2022-03-04" | "" |
      | "2021-12-31" | "2022-03-05" | "" |