Feature: Deadlines
  Scenario Outline: Adding valid deadlines
    Given the parent project starts at <ParentStart> and ends on <ParentEnd>
    When the user creates a deadline called <Name> on <Date>, with a description <Desc>
    Then a deadline called <Name> exists on <Date>, with a description <Desc>

    Examples:
      | ParentStart  | ParentEnd    | Date               | Name                               | Desc                                 |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "Eating Burgers"                   | "To Hide The Pain"                   |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "The string is 32 chars long....." | "Exact right name length - 32 chars" |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "Go"                               | "Exact right name length - 2 chars"  |


  Scenario Outline: Adding invalid deadlines
    Given the parent project starts at <ParentStart> and ends on <ParentEnd>
    When the user creates a deadline called <Name> on <Date>, with a description <Desc>
    Then creating the deadline should fail

    Examples:
      | ParentStart  | ParentEnd    | Date               | Name                                    | Desc                                  |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "qwertyuiopasdfghjklzxcvbnmqwertyuiop"  | "Name too long"                       |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "D"                                     | "Name too short"                      |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "      "                                | "Name is blank"                       |
      | "2022-01-01" | "2022-12-31" | "2021-02-20 12:30" | "D4"                                    | "Date before project start date"      |
      | "2022-01-01" | "2022-12-31" | "2023-02-20 12:30" | "D5"                                    | "Date after project end date"         |
