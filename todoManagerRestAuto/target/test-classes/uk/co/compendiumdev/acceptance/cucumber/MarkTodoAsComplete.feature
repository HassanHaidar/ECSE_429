Feature: Mark a todo as complete
  As a user I want to change a todo in case I made a mistake when initially creating it

  Background:
    Given The application is running
    And Todos exist:
      | todo_title            | status  | todo_desc             |
      | Prank Clowns          | false   | water interior plants |
      | Have fun              | false   | asssignment 2         |
      | Think deeply          | false   | Quiz 1                |

  Scenario Outline: The user successfully changes the status of a todo to complete (Normal Flow)
    Given The todo "<title>" exists
    When I change the todo title "<title>" to complete
    Then I should receive a confirmation that my operation was successful
    And todo "<title>"'s status should be true

    Examples:
      | title               |
      | Prank Clowns        |
      | Have fun            |
      | Think deeply        |

  Scenario Outline: The user successfully changes the title and description of a todo (Alternate Flow)
    Given The todo "<title>" exists
    When I change the todo title "<title>" to complete and the description to "<new_desc>"
    Then I should receive a confirmation that my operation was successful
    And todo "<title>" should have status true and description "<new_desc>" should show

    Examples:
      | title                | new_desc |
      | Prank Clowns         | nah      |
      | Have fun             | I'm      |
      | Think deeply         | trying   |

  Scenario Outline: The user attempts to mark the todo incorrectly (Error Flow)
    Given The todo "<title>" exists
    When I try to change the status of title "<title>" to "<new_status>"
    Then I should receive an error informing me that the passed information was invalid

    Examples:
      | title           | new_status       |
      | Prank Clowns    | trueeeee         |
      | Have fun        | 0                |


