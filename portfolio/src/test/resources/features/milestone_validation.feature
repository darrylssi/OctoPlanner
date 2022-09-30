Feature: Milestone validation
  Scenario Outline: Adding valid milestones
    Given the parent project starts at <ParentStart> and ends on <ParentEnd>
    When the user creates a milestone called <Name>, on <Date>, with a description <Description>
    Then there are no errors in creating the milestone

    Examples:
      | ParentStart  | ParentEnd    | Date         | Name                               | Description                          |
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "Sprint 1 Done"                    | "Deliverables delivered"             |
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "Sprint 2 demo retrospective done" | "Exact right name length - 32 chars" |
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "S3"                               | "Exact right name length - 2 chars"  |
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "No description"                   | ""  |
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "Milestone"                        | "This description is exactly 200 characters long. It only contains valid characters, like 13456789 and !@#%:'&*( and characters from other languages like サンジ and 전정국! This test will pass. Yes, it will!"  |

  Scenario Outline: Adding invalid milestones
    Given the parent project starts at <ParentStart> and ends on <ParentEnd>
    When the user creates a milestone called <Name>, on <Date>, with a description <Description>
    Then creating the milestone should have <Error>

    Examples:
      | ParentStart  | ParentEnd    | Date         | Name                                   | Description                  | Error |
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "qwertyuiopasdfghjklzxcvbnmqwertyuiop" | "Name too long"              | "The milestone name must be between 2 and 32 characters."|
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "F"                                    | "Name too short"             | "The milestone name must be between 2 and 32 characters."|
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "      "                               | "Name is blank"              | "Milestone name cannot be blank."|
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "S3"                                   | "This is a really really long milestone description that is hopefully longer than the maximum limit of two hundred characters which is a lot longer than I thought it would be so here are few more characters"  | "The milestone description must not exceed 200 characters."|
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "Emoji"                                | "Fire emoji 🔥"              | "Description can only have letters, numbers, punctuations and spaces."|
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "Invalid punctuation"                  | "This $ is not accepted for some reason ^" | "Description can only have letters, numbers, spaces and punctuation."|
      | "2022-01-01" | "2022-12-31" | "2021-02-20" | "M4"                                   | "Date before project start date" | "Milestone date must be within project date range: %s - %s" |
      | "2022-01-01" | "2022-12-31" | "2023-02-20" | "M5"                                   | "Date after project end date"    | "Milestone date must be within project date range: %s - %s" |
