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
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "No description"                   | ""  |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "Event"                            | "This description is exactly 200 characters long. It only contains valid characters, like 13456789 and !@#%:'&*( and characters from other languages like サンジ and 전정국! This test will pass. Yes, it will!"  |


  Scenario Outline: Adding invalid events
    Given the parent project starts at <ParentStart> and ends on <ParentEnd>
    When the user creates an event called <Name>, starting at <EventStart>, ending on <EventEnd>, with a description <Desc>
    Then creating the event should fail

    Examples:
      | ParentStart  | ParentEnd    | EventStart         | EventEnd           | Name                                   | Desc                         |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "qwertyuiopasdfghjklzxcvbnmqwertyuiop" | "Name too long"              |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "F"                                    | "Name too short"             |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "      "                               | "Name is blank"              |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "Emoji"                                | "Fire emoji 🔥"              |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "Invalid punctuation"                  | "This $ is not accepted for some reason ^" |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2022-02-20 13:00" | "Long description"                     | "This is a really really long milestone description that is hopefully longer than the maximum limit of two hundred characters which is a lot longer than I thought it would be so here are few more characters"              |
      | "2022-01-01" | "2022-12-31" | "2021-02-20 12:30" | "2022-02-20 13:00" | "Early start"                          | "Starts before project"      |
      | "2022-01-01" | "2022-12-31" | "2021-02-20 12:30" | "2021-02-20 13:00" | "Early event"                          | "Entirely before project"    |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2023-02-20 13:00" | "Late end"                             | "Ends after project"         |
      | "2022-01-01" | "2022-12-31" | "2023-02-20 12:30" | "2023-02-20 13:00" | "Late event"                           | "Entirely after project"     |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "2023-01-20 13:00" | "Wrong order"                          | "Event starts after it ends" |