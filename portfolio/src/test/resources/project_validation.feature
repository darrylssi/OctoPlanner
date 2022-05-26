Feature: The projects dates are changed

  Scenario Outline:
    Given The project has the following sprints with dates
      | SprintStart | SprintEnd   |
      | 2022-01-01  | 2022-02-02  |
      | 2022-02-06  | 2022-03-04  |
    When I set the project start date to <StartDate>
    And I set the project end date to <EndDate>
    Then <Outcome> message should be displayed

    Examples:
      | StartDate  | EndDate    | Outcome |
      | 2022-02-04 | 2022-08-05 | "The sprint with dates: 01/Jan/2022 - 02/Feb/2022 is outside the project dates |
      | 2022-01-20 | 2022-08-20 | "The sprint with dates: 01/Jan/2022 - 02/Feb/2022 is outside the project dates |
      | 2022-01-01 | 2022-02-10 | "The sprint with dates: 06/Feb/2022 - 04/Mar/2022 is outside the project dates |