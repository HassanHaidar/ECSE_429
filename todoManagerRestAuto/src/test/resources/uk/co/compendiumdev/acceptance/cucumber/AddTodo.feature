Feature: Create new Todo
  As a user I want to create new todos so that I can better organize my responsibilities.

  Background:
    Given The application is running
#    And   Todos exist:
#      | todo_title   | description |
#      | do project   |             |

  Scenario Outline: The user successfully adds a new category with a description (Normal Flow)
    When I add a todo title"<todo_title>" with "<description>" as description and status "<status>"
    Then I should receive a confirmation that my operation was successful
    And Todo "<todo_title>" with description "<description>" and status "<status>" should show

    Examples:
      | todo_title  | description                         | status  |
      | Vacuuming   | I need to vacuum the living room    | true    |
      | Study for quiz  | Chapters 1-5                    | false   |
      | Pet the dog  | He's a good boy                    | true    |

  Scenario Outline: The user successfully adds a new category without a description or status (Alternate Flow)
    When I add a todo "<todo_title>"
    Then I should receive a confirmation that my operation was successful
    And The Todo "<todo_title>" should show

    Examples:
      | todo_title  |
      | Clean desk       |
      | Create step definition      |
      | Write this todo |

  Scenario Outline: The user attempts to add a todo with a bad title (Error Flow)
    When I add todo "<todo_title>" with "<description>" as description and status "<status>"
    Then I should receive an error informing me that the passed information was invalid

    Examples:
      | todo_title | description | status |
      |             | home chores | true        |

