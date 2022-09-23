Feature: As a teacher, I can create a group
  Scenario Outline: Creating a valid new group
    When I try to create a group with short name <ShortName> and long name <LongName>
    Then the group should be saved to the database

    Examples:
      | ShortName | LongName |
      | "2c" | "" |
      | "Thirty-Two Character Long Name32" | "A valid long name" |
      | "A valid short name"               | "One hundred and twenty-eight character long long name Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod te" |

  Scenario Outline: Creating an invalid new group
    When I try to create a group with short name <ShortName> and long name <LongName>
    Then the group should not be saved to the database

    Examples:
      | ShortName | LongName |
      | "" | "A valid long name" |
      | "1" | "A valid long name" |
      | "Thirty-Three Character Long Name3" | "A valid long name" |
      | "A valid short name"                | "One hundred and twenty-nine character long long name Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod temp" |
      | "Much more than thirty-two character long name" | "A valid long name"                                                                                                     |
      | "A valid short name"                            | "Much more than one hundred and twenty-eight character long name Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum " |