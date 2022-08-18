Feature: Milestone validation
  Scenario Outline: Adding valid milestones
    Given the parent project starts at <ParentStart> and ends on <ParentEnd>
    When the user creates a milestone called <Name>, on <Date>, with a description <Description>
    Then a milestone called <Name> exists on <Date>, with a description <Description>

    Examples:
      | ParentStart  | ParentEnd    | Date         | Name                               | Description                          |
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "Sprint 1 Done"                    | "Deliverables delivered"             |
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "Sprint 2 demo retrospective done" | "Exact right name length - 32 chars" |
      | "2022-01-01" | "2022-12-31" | "2022-02-20" | "S3"                               | "Exact right name length - 2 chars"  |

  Scenario Outline: Adding invalid milestones
    Given the parent project starts at <ParentStart> and ends on <ParentEnd>
    When the user creates a milestone called <Name>, on <Date>, with a description <Description>
    Then creating the milestone should fail

    Examples:
      | ParentStart  | ParentEnd    | Date               | Name                                   | Description                  |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "qwertyuiopasdfghjklzxcvbnmqwertyuiop" | "Name too long"              |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "F"                                    | "Name too short"             |
      | "2022-01-01" | "2022-12-31" | "2022-02-20 12:30" | "      "                               | "Name is blank"              |