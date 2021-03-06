Feature: Create new Category
  As a user I want to create new categories so that I can organize my projects better.

  Background:
    Given The application is running
    And   Categories exist:
      | cat_title   | description |
      | Summer Trip |             |

  Scenario Outline: The user successfully adds a new category with a description (Normal Flow)
    When I add a category title"<cat_title>" with "<description>" as description
    Then I should receive a confirmation that my operation was successful
    And Category "<cat_title>" with description "<description>" should show

    Examples:
      | cat_title  | description     |
      | Home       | home chores     |
      | Work       | work related    |
      | University | university work |

  Scenario Outline: The user successfully adds a new category without a description (Alternate Flow)
    When I add a category title"<cat_title>"
    Then I should receive a confirmation that my operation was successful
    And Category "<cat_title>"  should show

    Examples:
      | cat_title  |
      | Home       |
      | Work       |
      | University |

  Scenario Outline: The user attempts to change the category without a title (Error Flow)
    When I add a category with only a description "<description>"
    Then I should receive an error informing me that the passed information was invalid

    Examples:
      | description |
      | home chores |


