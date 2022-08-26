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

  Scenario Outline: Adding invalid milestones
    Given the parent project starts at <ParentStart> and ends on <ParentEnd>
    When the user creates a milestone called <Name>, on <Date>, with a description <Description>
    Then creating the milestone should have <Error>

    Examples:
      | ParentStart  | ParentEnd    | Date               | Name                                   | Description                  | Error |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "qwertyuiopasdfghjklzxcvbnmqwertyuiop" | "Name too long"              | "The milestone name must be between 2 and 32 characters."|
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "F"                                    | "Name too short"             | "The milestone name must be between 2 and 32 characters."|
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "      "                               | "Name is blank"              | "Milestone name cannot be blank."|
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "S3"                               | "This is a really really long milestone description that is hopefully longer than the maximum limit of two hundred characters which is a lot longer than I thought it would be so here are few more characters"  | "The milestone description must not exceed 200 characters."|