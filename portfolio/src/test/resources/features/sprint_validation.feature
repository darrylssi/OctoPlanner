Feature: A sprint's dates are changed

  Scenario Outline:
    Given The project has the following sprints with dates
      | 2022-01-01  | 2022-02-02  |
      | 2022-02-06  | 2022-03-04  |
    And The projects creation date is "2022-01-01"
    When I set a sprint's start date to <StartDate>
    And I set a sprint's end date to <EndDate>
    Then Sprint <Outcome> message should be displayed

    Examples:
      | StartDate  | EndDate    | Outcome |
      | "2021-12-31" | "2022-02-02" | "Sprint dates must be within project date range: 01/Jan/2022 - 01/Oct/2022" |
      | "2022-11-02" | "2023-01-02" | "Sprint dates must be within project date range: 01/Jan/2022 - 01/Oct/2022" |
      | "2022-04-01" | "2022-05-01" | "" |
      | "2022-02-05" | "2022-04-02" | "Sprint dates must not overlap with other sprints. Dates are overlapping with 06/Feb/2022 - 04/Mar/2022" |
      | "2022-02-06" | "2022-04-02" | "Sprint dates must not overlap with other sprints. Dates are overlapping with 06/Feb/2022 - 04/Mar/2022" |
      | "2022-02-07" | "2022-04-02" | "Sprint dates must not overlap with other sprints. Dates are overlapping with 06/Feb/2022 - 04/Mar/2022" |
      | "2022-03-03" | "2022-04-02" | "Sprint dates must not overlap with other sprints. Dates are overlapping with 06/Feb/2022 - 04/Mar/2022" |
      | "2022-02-01" | "2022-02-05" | "Sprint dates must not overlap with other sprints. Dates are overlapping with 01/Jan/2022 - 02/Feb/2022" |
      | "2022-05-01" | "2022-04-01" | "Start date must always be before end date" |