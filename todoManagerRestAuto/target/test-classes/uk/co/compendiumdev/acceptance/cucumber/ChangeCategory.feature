Feature: Change a Category
  As a user I want to change a category so that I can modify it in case I made a mistake or want to refine its
  description.

  Background:
    Given The application is running
    And   Categories exist:
      | cat_title   | description     |
      | Summer Trip |                 |
      | Home        | home chores     |
      | Work        | work related    |
      | University  | university work |

  Scenario Outline: The user successfully changes the title of a category (Normal Flow)
    Given The category "<cat_title>" exists
    When I change the category title "<cat_title>" to "<new_title>"
    Then I should receive a confirmation that my operation was successful
    And Category "<cat_title>" with updated title "<new_title>" should show

    Examples:
      | cat_title  | new_title  |
      | Home       | HomeChores |
      | Work       | workAssgn  |
      | University | McGill     |

  Scenario Outline: The user successfully changes the title and description of a category (Alternate Flow)
    Given The category "<cat_title>" exists
    When I change the category title "<cat_title>" to "<new_title>" and the description to "<new_desc>"
    Then I should receive a confirmation that my operation was successful
    And Category "<cat_title>" with updated title "<new_title>" and description "<new_desc>" should show

    Examples:
      | cat_title   | new_title  | new_desc         |
      | Home        | HomeChores | grocery shopping |
      | Work        | workAssgn  | Internship work  |
      | Summer Trip | Vacation   | 2021 vacation    |

  Scenario Outline: The user attempts to change the title of a category that does not exist (Error Flow)
    Given The category "<cat_title>" does not exist
    When I try to change the non existent category title "<cat_title>" to "<new_title>"
    Then I should receive an error informing me that the requested resource was not found

    Examples:
      | cat_title   | new_title       |
      | home chores | Groceries       |
      | Fitness     | Extracurricular |


