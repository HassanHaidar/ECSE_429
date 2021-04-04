Feature: Filter todos by category
  As a user I want to order todos by category to see more clearly which Todos belong to which category.

  Background:
    Given The application is running
    And   Categories exist:
      | cat_title   | description     |
      | Summer Trip |                 |
      | Home        | home chores     |
      | Work        | work related    |
      | University  | university work |
    And Todos exist:
      | todo_title   | status  | todo_desc             |
      | Water plants | false   | water interior plants |
      | ECSE429 asgn | true    | asssignment 2         |
      | ESCE444      | false   | Quiz 1                |



  Scenario Outline: The user successfully assigns a non existing todo for the chosen category (Normal Flow)
    Given The category "<cat_title>" exists
    And the Todo "<todo_title>" does not exist
    When I add the new Todo "<todo_title>" with status "<status>" and "<todo_desc>"under the category "<cat_title>"
    Then I should receive a confirmation that my operation was successful
    And The Todo "<todo_title>" under category "<cat_title>" should show
    And The Todo "<todo_title>" should show

    Examples:
      | cat_title   | todo_title   | status | todo_desc   |
      | Home        | Feed cat     | false  | feed nelson |
      | University  | ECSE417      | true   | quiz1       |
      | University  | ESCE446      | false  | midterm     |

  Scenario Outline: The user successfully assigns a non existing todo with only the title for the chosen category
  (Alternate Flow)
    Given The category "<cat_title>" exists
    And the Todo "<todo_title>" does not exist
    When I add the new Todo "<todo_title>" under the category "<cat_title>"
    Then I should receive a confirmation that my operation was successful
    And The Todo "<todo_title>" under category "<cat_title>" should show
    And The Todo "<todo_title>" should show

    Examples:
      | cat_title   | todo_title   |
      | Home        | Feed cat     |
      | University  | ECSE417      |
      | University  | ESCE446      |

  Scenario Outline: The user successfully assigns an existing todo for the chosen category (Alternate Flow)
    Given The category "<cat_title>" exists
    And The Todo "<todo_title>" exists
    When I add a Todo "<todo_title>" under the category "<cat_title>"
    Then I should receive a confirmation that my operation was successful
    And The Todo "<todo_title>" under category "<cat_title>" should show

    Examples:
      | cat_title   | todo_title   |
      | Home        | Water plants |
      | University  | ECSE429 asgn |
      | University  | ESCE444      |

  Scenario Outline: The user attempts to assign a non existing todo without title for the chosen category (Error Flow)
    Given The category "<cat_title>" exists
    When I add the new Todo with description "<description>" under the category "<cat_title>"
    Then I should receive an error informing me that the passed information was invalid

    Examples:
      | cat_title   | description |
      | Home        | home chores |
      | University  | uni         |


