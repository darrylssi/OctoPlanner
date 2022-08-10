Feature: Events
  Scenario Outline: Adding valid events
    Given the parent project starts at <ParentStart> and ends on <ParentEnd>
    When the user creates an event called <Name>, starting at <EventStart>, ending on <EventEnd>, with a description <Desc>
    Then an event called <Name> exists starting at <EventStart>, ending at <EventEnd>, with a description <Desc>

    Examples:
      | ParentStart  | ParentEnd    | EventStart         | EventEnd           | Name                               | Desc                                 |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "Eating Burgers"                   | "To Hide The Pain"                   |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "The string is 32 chars long....." | "Exact right name length - 32 chars" |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "Go"                               | "Exact right name length - 2 chars"  |


  Scenario Outline: Adding invalid events
    Given the parent project starts at <ParentStart> and ends on <ParentEnd>
    When the user creates an event called <Name>, starting at <EventStart>, ending on <EventEnd>, with a description <Desc>
    Then creating the event should fail

    Examples:
      | ParentStart  | ParentEnd    | EventStart         | EventEnd           | Name                                   | Desc             |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "qwertyuiopasdfghjklzxcvbnmqwertyuiop" | "Name too long"  |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "a" | "Name too short" |
      # | "2022-01-01" | "2022-12-31" | "2021-02-20 12:30" | "2022-02-20 13:00" | "Fail"                                 | "Starts before project"   |
      # | "2022-01-01" | "2022-12-31" | "2021-02-20 12:30" | "2021-02-20 13:00" | "Fail"                                 | "Entirely before project" |
      # | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2023-02-20 13:00" | "Fail"                                 | "Ends after project"      |
      # | "2022-01-01" | "2022-12-31" | "2023-02-20 12:30" | "2023-02-20 13:00" | "Fail"                                 | "Entirely after project"  |
      # | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "F"                                    | "Name too short"          |