Feature: Change a todo
  As a user I want to change a todo to fix a mistake when initially creating it.

  Background:
    Given The application is running
    And Todos exist:
      | todo_title            | status  | todo_desc             |
      | Prank Clowns          | false   | water interior plants |
      | Have fun              | true    | asssignment 2         |
      | Think deeply          | false   | Quiz 1                |

  Scenario Outline: The user successfully changes the title of a todo (Normal Flow)
    Given The todo "<title>" exists
    When I change the todo title "<title>" to "<new_title>"
    Then I should receive a confirmation that my operation was successful
    And todo "<title>" with updated title "<new_title>" should show

    Examples:
      | title               | new_title     |
      | Prank Clowns        | Clown pranks  |
      | Have fun            | Study harder  |
      | Think deeply        | Philosophize  |

  Scenario Outline: The user successfully changes the title and description of a todo (Alternate Flow)
    Given The todo "<title>" exists
    When I change the todo title "<title>" to "<new_title>" and the description to "<new_desc>"
    Then I should receive a confirmation that my operation was successful
    And todo "<title>" with updated title "<new_title>" and description "<new_desc>" should show

    Examples:
      | title               | new_title     | new_desc |
      | Prank Clowns        | Clown pranks  | nah      |
      | Have fun            | Study harder  | I'm      |
      | Think deeply        | Philosophize  | trying   |

  Scenario Outline: The user attempts to change the title of a todo that does not exist (Error Flow)
    Given The todo "<title>" does not exist
    When I try to change the non existent todo title "<title>" to "<new_title>"
    Then I should receive an error informing me that the requested resource was not found

    Examples:
      | title           | new_title       |
      | Take a bathy    | Shower          |
      | Stay cool       | Extracurricular |


